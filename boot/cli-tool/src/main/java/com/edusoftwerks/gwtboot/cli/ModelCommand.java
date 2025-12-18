package com.edusoftwerks.gwtboot.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

@Command(
        name = "model",
        description = "Create a new domain model",
        mixinStandardHelpOptions = true
)
public class ModelCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The model name")
    private String modelName;

    @Override
    public Integer call() throws Exception {
        if (modelName == null || modelName.trim().isEmpty()) {
            Console.error("Model name is required");
            Console.println("Usage: gwt-boot model <model-name>");
            Console.println("Example: gwt-boot model User");
            return 1;
        }

        // Capitalize model name for class namesf
        String modelClass = Utils.capitalize(modelName);

        // Check if we're in a GWT Boot project
        Path pomPath = Paths.get("pom.xml");
        if (!Files.exists(pomPath)) {
            Console.error("pom.xml not found. Make sure you're in the root of a GWT Boot project.");
            return 1;
        }

        // Extract package name from pom.xml
        String packageName = Utils.extractPackageFromPom(pomPath);
        if (packageName == null) {
            Console.error("Could not determine package from pom.xml");
            return 1;
        }

        // Convert package to directory path
        String packagePath = packageName.replace('.', '/');

        // Define base paths
        Path javaBase = Paths.get("src/main/java", packagePath);

        Console.info("===================================");
        Console.info("Model Generator");
        Console.info("===================================");
        Console.println("");
        Console.println("Model name: " + modelClass);
        Console.println("Package: " + packageName);
        Console.println("");

        // Check for existing files

        Path sharedModelsDir = javaBase.resolve("shared/models");

        java.util.List<Path> existingFiles = new java.util.ArrayList<>();

        Path modelFile = sharedModelsDir.resolve(modelClass + ".java");
        if (Files.exists(modelFile)) existingFiles.add(modelFile);

        if (!existingFiles.isEmpty()) {
            Console.warning("WARNING: The following files will be overwritten:");
            for (Path file : existingFiles) {
                Console.println("  - " + file);
            }
            Console.println("");

            if (!InputReader.confirm("Continue with generation?", false)) {
                Console.warning("Model generation cancelled.");
                return 1;
            }
            Console.println("");
        }

        // Create package directories
        Console.info("Creating package structure...");
        Files.createDirectories(sharedModelsDir);

        // Create model
        Console.info("Creating " + modelClass + ".java...");
        Files.writeString(modelFile,
                String.format("""
                        package %s.shared.models;

                        import com.google.gwt.user.client.rpc.IsSerializable;

                        public class %s implements IsSerializable {

                            private Long id;

                            // Add your Model fields here
                            // Example:
                            // private String name;

                            public %s() {
                            }

                            public Long getId() {
                                return id;
                            }

                            // Add getters and setters for your fields
                        }
                        """, packageName, modelClass, modelClass)
        );

        Console.println("");
        Console.success("===================================");
        Console.success("Domain created successfully!");
        Console.success("===================================");
        Console.println("");
        Console.println("Created:");
        Console.println("  - " + sharedModelsDir.resolve(modelClass + ".java"));
        Console.println("");
        Console.println("Next steps:");
        Console.println("  1. Add fields to " + modelClass + " model");
        Console.println("");

        return 0;
    }


}
