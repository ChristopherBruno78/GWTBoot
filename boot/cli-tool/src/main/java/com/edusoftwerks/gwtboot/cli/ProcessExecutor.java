package com.edusoftwerks.gwtboot.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ProcessExecutor {

    private static String archetypeVersion = "1.0.0";

    public static int executeMavenArchetype(
            String groupId,
            String artifactId,
            String version,
            String packageName
    ) throws IOException, InterruptedException {

        List<String> command = new ArrayList<>();
        command.add("mvn");
        command.add("archetype:generate");
        command.add("-DarchetypeGroupId=com.edusoftwerks");
        command.add("-DarchetypeArtifactId=gwt-boot-archetype");
        command.add("-DarchetypeVersion=" + archetypeVersion);
        command.add("-DgroupId=" + groupId);
        command.add("-DartifactId=" + artifactId);
        command.add("-Dversion=" + version);
        command.add("-Dpackage=" + packageName);
        command.add("-DinteractiveMode=false");

        return executeCommand(command);
    }

    public static int executeCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        pb.inheritIO();

        Process process = pb.start();
        return process.waitFor();
    }

    public static void makeExecutable(String filePath) throws IOException, InterruptedException {
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Windows doesn't need chmod
            return;
        }

        List<String> command = new ArrayList<>();
        command.add("chmod");
        command.add("+x");
        command.add(filePath);

        executeCommand(command);
    }
}
