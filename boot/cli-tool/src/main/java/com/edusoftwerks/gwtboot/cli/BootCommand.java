package com.edusoftwerks.gwtboot.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(
    name = "boot",
    description = "Generate a new GWT Boot project from archetype",
    mixinStandardHelpOptions = true
)
public class BootCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The artifact ID for the project", arity = "0..1")
    private String artifactId;

    @Override
    public Integer call() throws Exception {
        Console.info("===================================");
        Console.info("GWT Boot Project Generator");
        Console.info("===================================");
        Console.println("");


        // Prompt for groupId
        String groupId = InputReader.readLine("Enter groupId (e.g., com.mycompany): ");
        if (groupId == null || groupId.trim().isEmpty()) {
            Console.error("groupId cannot be empty");
            return 1;
        }
        groupId = groupId.trim().toLowerCase();

        // Prompt for artifactId if not provided as argument
        if (artifactId == null || artifactId.trim().isEmpty()) {
            artifactId = InputReader.readLine("Enter artifactId (e.g., myapp): ");
            if (artifactId == null || artifactId.trim().isEmpty()) {
                Console.error("artifactId cannot be empty");
                return 1;
            }
            artifactId = artifactId.trim();
        }

        artifactId = artifactId.toLowerCase();

        // Prompt for version (with default)
        String version = InputReader.readLineWithDefault(
            "Enter version (default: 0.0.1-SNAPSHOT): ",
            "0.0.1-SNAPSHOT"
        );

        // Prompt for package (with default derived from groupId.artifactId)
        String defaultPackage = groupId + "." + artifactId;
        String packageName = InputReader.readLineWithDefault(
            "Enter package name (default: " + defaultPackage + "): ",
            defaultPackage
        );

        Console.println("");
        Console.info("Summary:");
        Console.println("--------");
        Console.println("groupId:    " + groupId);
        Console.println("artifactId: " + artifactId);
        Console.println("version:    " + version);
        Console.println("package:    " + packageName);
        Console.println("");

        boolean confirmed = InputReader.confirm("Generate project with these settings?", true);
        if (!confirmed) {
            Console.warning("Project generation cancelled.");
            return 0;
        }

        Console.println("");
        Console.info("Generating project...");
        Console.println("");

        int exitCode = ProcessExecutor.executeMavenArchetype(groupId, artifactId, version, packageName);

        if (exitCode == 0) {
            // Fix line endings and make scripts executable
            Path mvnwPath = Paths.get(artifactId, "mvnw");
            if (Files.exists(mvnwPath)) {
                ProcessExecutor.makeExecutable(mvnwPath.toString());
            }

            Path mvnwCmdPath = Paths.get(artifactId, "mvnw.cmd");
            if (Files.exists(mvnwCmdPath)) {
                ProcessExecutor.makeExecutable(mvnwCmdPath.toString());
            }

            Console.println("");
            Console.success("===================================");
            Console.success("Project generated successfully!");
            Console.success("===================================");
            Console.println("");
        } else {
            Console.println("");
            Console.error("Project generation failed");
            return 1;
        }

        return 0;
    }
}
