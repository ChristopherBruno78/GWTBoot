package com.edusoftwerks.gwtboot.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.nio.file.StandardWatchEventKinds.*;

@Command(
        name = "dev",
        description = "Launch the GWT CodeServer for development",
        mixinStandardHelpOptions = true
)
public class DevCommand implements Callable<Integer> {
    @Option(
            names = {"-m", "--memory"},
            description = "Maximum heap size in MB (default: ${DEFAULT-VALUE})",
            defaultValue = "2048"
    )
    private int maxMemoryMb;

    // Shared state for tracking hot reload timing
    private volatile boolean waitingForRestart = false;
    private volatile long reloadStartTime = 0;

    @Override
    public Integer call() throws Exception {
        long codeServerStartTime = System.currentTimeMillis();

        Console.info("===================================");
        Console.info("Starting GWT CodeServer");
        Console.info("===================================");
        Console.println("");

        // Check if CodeServer is already running
        Path pidFile = Paths.get("target/gwt-codeserver.pid");
        if (Files.exists(pidFile)) {
            try {
                String pidString = Files.readString(pidFile).trim();
                long pid = Long.parseLong(pidString);

                // Check if process is still running
                if (isProcessRunning(pid)) {
                    Console.error("GWT CodeServer is already running (PID: " + pid + ")");
                    Console.error("Stop it first using: kill -9 " + pid);
                    return 1;
                }

                // PID file exists but process is dead, clean it up
                Files.delete(pidFile);
                Console.info("Cleaned up stale CodeServer PID file");
            } catch (NumberFormatException | IOException e) {
                // Invalid or unreadable PID file, delete it
                try {
                    Files.delete(pidFile);
                } catch (IOException ex) {
                    // Ignore
                }
            }
        }

        // Check if pom.xml exists in current directory
        Path pomPath = Paths.get("pom.xml");
        if (!Files.exists(pomPath)) {
            Console.error("No pom.xml found in current directory");
            Console.error("Please run this command from your GWT Boot project root");
            return 1;
        }

        // Extract GWT version from pom.xml
        String gwtVersion = Utils.extractGwtVersionFromPom(pomPath);
        if (gwtVersion == null) {
            Console.error("Could not determine GWT version from pom.xml");
            return 1;
        }

        Console.info("Using GWT version: " + gwtVersion);

        // Find all GWT modules
        List<String> moduleNames = Utils.findGwtModules();
        if (moduleNames.isEmpty()) {
            Console.error(
                    "Could not find any GWT modules (*.gwt.xml) in src/main/java"
            );
            return 1;
        }

        Console.info("Found " + moduleNames.size() + " GWT module(s):");
        for (String module : moduleNames) {
            Console.info("  - " + module);
        }
        Console.println("");

        // Build classpath
        String classpath = Utils.buildClasspath(gwtVersion);
        if (classpath == null) {
            Console.error("Could not find gwt-codeserver jar in Maven repository");
            Console.error(
                    "Please ensure GWT is installed in your local Maven repository"
            );
            return 1;
        }

        Console.info("Max heap memory: " + maxMemoryMb + "MB");
        Console.info("Launching GWT CodeServer (SuperDevMode) in background...");
        Console.println("");
        Console.println("=== GWT CodeServer Output ===");
        Console.println("");

        Process process = executeGwtCodeServer(classpath, moduleNames, maxMemoryMb);
        long pid = process.pid();

        // Give the CodeServer a moment to start
        Thread.sleep(2000);

        // Check if process died immediately
        if (!process.isAlive()) {
            Console.println("");
            Console.error("GWT CodeServer failed to start!");
            Console.error("Check the error messages above for details");
            return 1;
        }

        // Save the PID for later reference
        pidFile = Paths.get("target/gwt-codeserver.pid");
        Files.createDirectories(pidFile.getParent());
        Files.writeString(pidFile, String.valueOf(pid));

        Console.println("");
        Console.println("=============================");
        Console.println("");
        Console.success("GWT CodeServer started successfully!");
        Console.info("CodeServer URL: http://localhost:9876/");
        Console.info("Process ID: " + pid);
        Console.println("");

        // Add shutdown hook to clean up CodeServer if interrupted
        final Process codeServerProcess = process;
        final Path codeServerPidFile = pidFile;
        Runtime
                .getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    try {
                                        if (codeServerProcess.isAlive()) {
                                            codeServerProcess.destroyForcibly();
                                        }
                                        if (Files.exists(codeServerPidFile)) {
                                            Files.delete(codeServerPidFile);
                                        }
                                    } catch (Exception e) {
                                        // Ignore exceptions during shutdown
                                    }
                                }
                        )
                );

        // Start file watcher (excluding client package)
        Thread watcherThread = new Thread(
                () -> {
                    try {
                        watchFileChanges();
                    } catch (Exception e) {
                        Console.error("File watcher error: " + e.getMessage());
                    }
                }
        );
        watcherThread.setDaemon(true);
        watcherThread.start();

        // Calculate CodeServer startup time
        long codeServerEndTime = System.currentTimeMillis();
        long codeServerElapsedMs = codeServerEndTime - codeServerStartTime;
        double codeServerElapsedSec = codeServerElapsedMs / 1000.0;

        // Start Spring Boot (runs in foreground, blocking)
        Console.info("Starting Spring Boot application...");
        Console.println("");
        Console.println("=== Spring Boot Output ===");
        Console.println("");

        return executeSpringBoot(codeServerElapsedSec, codeServerElapsedMs);
    }

    private Process executeGwtCodeServer(
            String classpath,
            List<String> moduleNames,
            int maxMemoryMb
    )
            throws IOException {
        List<String> command = new ArrayList<>();
        command.add("java");
        command.add("-Xmx" + maxMemoryMb + "m");
        command.add("-cp");
        command.add(classpath);
        command.add("com.google.gwt.dev.codeserver.CodeServer");
        command.add("-src");
        command.add("src/main/java");
        command.add("-launcherDir");
        command.add("target/classes/static");
        command.add("-sourceLevel");
        command.add("17");
        command.add("-logLevel");
        command.add("INFO");

        // Add all module names to the command
        command.addAll(moduleNames);

        return ProcessExecutor.executeCommandInBackgroundWithOutput(command);
    }

    private boolean isProcessRunning(long pid) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder pb;

            if (os.contains("win")) {
                // Windows: tasklist /FI "PID eq <pid>"
                pb = new ProcessBuilder("tasklist", "/FI", "PID eq " + pid);
            } else {
                // Unix/Linux/Mac: ps -p <pid>
                pb = new ProcessBuilder("ps", "-p", String.valueOf(pid));
            }

            Process process = pb.start();
            int exitCode = process.waitFor();

            // Exit code 0 means the process exists
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            // If we can't check, assume it's not running
            return false;
        }
    }

    private static final String SPRING_BOOT_STARTED_CONSOLE_TRIGGER = "Started Application in";
    private static final String SPRING_BOOT_RESTARTED_CONSOLE_TRIGGER = "Condition evaluation unchanged";

    private int executeSpringBoot(
            double codeServerElapsedSec,
            long codeServerElapsedMs
    )
            throws InterruptedException, IOException {
        List<String> command = new ArrayList<>();
        command.add("mvn");
        command.add("spring-boot:run");

        long springBootStartTime = System.currentTimeMillis();
        boolean timingPrinted = false;

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        // Monitor the output to detect when Spring Boot is ready
        try (
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                )
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);

                // Detect when Spring Boot has finished starting (initial startup)
                if (!timingPrinted && line.contains(SPRING_BOOT_STARTED_CONSOLE_TRIGGER)) {
                    long springBootEndTime = System.currentTimeMillis();
                    long springBootElapsedMs = springBootEndTime - springBootStartTime;
                    double springBootElapsedSec = springBootElapsedMs / 1000.0;

                    // Print startup timing information
                    Console.info("");
                    Console.success("GWT-Boot dev server started at http://localhost:8080/");
                    Console.info("===================================");
                    Console.info("Startup Times:");
                    Console.info(
                            "  GWT CodeServer: " +
                                    String.format("%.2f", codeServerElapsedSec) +
                                    " seconds (" +
                                    codeServerElapsedMs +
                                    " ms)"
                    );
                    Console.info(
                            "  Spring Boot: " +
                                    String.format("%.2f", springBootElapsedSec) +
                                    " seconds (" +
                                    springBootElapsedMs +
                                    " ms)"
                    );
                    Console.info(
                            "  Total: " +
                                    String.format("%.2f", codeServerElapsedSec + springBootElapsedSec) +
                                    " seconds (" +
                                    (codeServerElapsedMs + springBootElapsedMs) +
                                    " ms)"
                    );
                    Console.info("===================================");
                    Console.info("");
                    Console.info("Watching for changes (excluding 'client' package)...");
                    Console.info("Press Ctrl+C to stop both servers.");
                    Console.info("");

                    timingPrinted = true;
                }
                // Detect when Spring Boot has finished restarting (hot reload)
                else if (waitingForRestart && line.contains(SPRING_BOOT_RESTARTED_CONSOLE_TRIGGER)) {
                    long reloadEndTime = System.currentTimeMillis();
                    long reloadElapsedMs = reloadEndTime - reloadStartTime;
                    double reloadElapsedSec = reloadElapsedMs / 1000.0;

                    // Print hot reload timing information
                    Console.info("");
                    Console.success("Spring Boot restart complete");
                    Console.info("===================================");
                    Console.info(
                            "Hot reload time: " +
                                    String.format("%.2f", reloadElapsedSec) +
                                    " seconds (" +
                                    reloadElapsedMs +
                                    " ms)"
                    );
                    Console.info("===================================");
                    Console.info("");
                    Console.info("Watching for changes (excluding 'client' package)...");
                    Console.info("Press Ctrl+C to stop both servers.");
                    Console.info("");

                    // Reset flag
                    waitingForRestart = false;
                }
            }
        }

        return process.waitFor();
    }

    private void watchFileChanges() throws IOException, InterruptedException {
        Path srcMainJava = Paths.get("src/main/java");
        if (!Files.exists(srcMainJava)) {
            Console.warning("Source directory not found, file watcher disabled");
            return;
        }

        WatchService watchService = FileSystems.getDefault().newWatchService();
        Map<WatchKey, Path> watchKeys = new HashMap<>();

        // Register all directories EXCEPT those containing "client"
        Files.walkFileTree(
                srcMainJava,
                new SimpleFileVisitor<>() {

                    @Override
                    public FileVisitResult preVisitDirectory(
                            Path dir,
                            BasicFileAttributes attrs
                    )
                            throws IOException {
                        String pathString = dir.toString();

                        // Skip if this path contains "client"
                        if (
                                pathString.contains("/client") || pathString.contains("\\client")
                        ) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }

                        // Register this directory
                        WatchKey key = dir.register(
                                watchService,
                                ENTRY_CREATE,
                                ENTRY_MODIFY,
                                ENTRY_DELETE
                        );
                        watchKeys.put(key, dir);
                        return FileVisitResult.CONTINUE;
                    }
                }
        );

        if (watchKeys.isEmpty()) {
            Console.warning("No directories to watch, file watcher disabled");
            return;
        }

        Console.info("Watching " + watchKeys.size() + " directories for changes");

        long lastCompileTime = 0;
        final long DEBOUNCE_MS = 5000;
        final long STARTUP_GRACE_PERIOD_MS = 5000; // Ignore events for first 5 seconds
        long watcherStartTime = System.currentTimeMillis();

        while (true) {
            WatchKey key = watchService.take();
            Path dir = watchKeys.get(key);

            if (dir == null) {
                key.reset();
                continue;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == OVERFLOW) {
                    continue;
                }

                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path filename = ev.context();
                Path changedFile = dir.resolve(filename);

                // Only react to .java files
                if (filename.toString().endsWith(".java")) {
                    long currentTime = System.currentTimeMillis();

                    // Skip events during startup grace period
                    if (currentTime - watcherStartTime < STARTUP_GRACE_PERIOD_MS) {
                        continue;
                    }

                    // Debounce: only compile if enough time has passed since last compile
                    if (currentTime - lastCompileTime > DEBOUNCE_MS) {
                        lastCompileTime = currentTime;

                        Console.println("");
                        Console.info("=============================");
                        Console.info("Detected change: " + changedFile.getFileName());
                        Console.info("Compiling...");
                        Console.info("=============================");

                        // Run mvn compile
                        try {
                            List<String> command = new ArrayList<>();
                            command.add("mvn");
                            command.add("compile");

                            ProcessBuilder pb = new ProcessBuilder(command);
                            pb.redirectErrorStream(true);
                            pb.inheritIO();

                            Process compileProcess = pb.start();
                            int exitCode = compileProcess.waitFor();

                            Console.println("");
                            if (exitCode == 0) {
                                Console.success("Compilation successful - waiting for Spring Boot restart...");
                                // Set flag to track restart timing
                                reloadStartTime = System.currentTimeMillis();
                                waitingForRestart = true;
                            } else {
                                Console.error("Compilation failed with exit code: " + exitCode);
                            }
                        } catch (IOException | InterruptedException e) {
                            Console.error("Compilation error: " + e.getMessage());
                        }

                        Console.println("");
                    }
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                watchKeys.remove(key);
                if (watchKeys.isEmpty()) {
                    break;
                }
            }
        }
    }
}
