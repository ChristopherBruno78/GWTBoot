package com.edusoftwerks.gwtboot.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        // Check for existing Entity class
        Path persistenceEntitiesDir = javaBase.resolve("persistence/entities");
        Path entityFile = persistenceEntitiesDir.resolve(modelClass + "Entity.java");

        List<FieldInfo> fields = new ArrayList<>();
        Set<String> imports = new HashSet<>();
        if (Files.exists(entityFile)) {
            Console.info("Found " + modelClass + "Entity.java - copying fields...");
            try {
                fields = parseEntityFields(entityFile);
                if (!fields.isEmpty()) {
                    Console.success("Found " + fields.size() + " field(s) to copy");
                    // Extract imports needed for the field types
                    imports = extractImportsForFields(entityFile, fields);
                }
                else {
                    Console.info("No fields found");
                }
            } catch (Exception e) {
                Console.warning("Could not parse entity fields: " + e.getMessage());
            }
        }

        // Create model
        Console.info("Creating " + modelClass + ".java...");

        String fieldDeclarations = generateFieldDeclarations(fields);
        String gettersSetters = generateAllGettersSetters(fields);
        String additionalImports = generateImports(imports);

        Files.writeString(modelFile,
                String.format("""
                        package %s.shared.models;

                        import com.google.gwt.user.client.rpc.IsSerializable;
                        %s
                        public class %s implements IsSerializable {

                            private Long id;

                        %s
                            public %s() {
                            }

                            public Long getId() {
                                return id;
                            }
                        %s}
                        """, packageName, additionalImports, modelClass, fieldDeclarations, modelClass, gettersSetters)
        );

        Console.println("");
        Console.success("===================================");
        Console.success("Model created successfully!");
        Console.success("===================================");
        Console.println("");
        Console.println("Created:");
        Console.println("  - " + sharedModelsDir.resolve(modelClass + ".java"));
        if (!fields.isEmpty()) {
            Console.println("");
            Console.println("Copied " + fields.size() + " field(s) from " + modelClass + "Entity");
        }
        Console.println("");
        if (fields.isEmpty()) {
            Console.println("Next steps:");
            Console.println("  1. Add fields to " + modelClass + " model");
            Console.println("");
        }

        return 0;
    }

    private static class FieldInfo {
        String type;
        String name;

        FieldInfo(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }

    private List<FieldInfo> parseEntityFields(Path entityPath) throws Exception {
        List<FieldInfo> fields = new ArrayList<>();
        String content = Files.readString(entityPath);

        // Pattern to match field declarations: private Type name;
        // This handles generic types like List<String>, Map<K,V>, etc.
        Pattern fieldPattern = Pattern.compile(
            "private\\s+([A-Za-z0-9_<>\\[\\],\\s]+)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*;",
            Pattern.MULTILINE
        );

        Matcher matcher = fieldPattern.matcher(content);
        int lastFieldEnd = 0;

        while (matcher.find()) {
            String type = matcher.group(1).trim();
            String name = matcher.group(2).trim();

            // Get the text between the last field and this field
            int fieldStart = matcher.start();
            String betweenFields = content.substring(lastFieldEnd, fieldStart);

            // Skip the id field explicitly (we generate it separately)
            if (name.equals("id")) {
                lastFieldEnd = matcher.end();
                continue;
            }

            // Check if THIS field has JPA annotations we want to skip
            // Only check the text immediately before this field (not previous fields)
            if (betweenFields.contains("@Id") ||
                betweenFields.contains("@GeneratedValue") ||
                betweenFields.contains("@Version")) {
                // Make sure the annotation is for THIS field by checking there's no other
                // field declaration between the annotation and our field
                if (!betweenFields.replaceAll("@Id", "")
                                  .replaceAll("@GeneratedValue", "")
                                  .replaceAll("@Version", "")
                                  .matches("(?s).*private\\s+.*")) {
                    lastFieldEnd = matcher.end();
                    continue;
                }
            }

            fields.add(new FieldInfo(type, name));
            lastFieldEnd = matcher.end();
        }

        return fields;
    }

    private String generateGetterSetter(FieldInfo field) {
        String capitalizedName = Utils.capitalize(field.name);

        return String.format("""

                    public %s get%s() {
                        return %s;
                    }

                    public void set%s(%s %s) {
                        this.%s = %s;
                    }
                """, field.type, capitalizedName, field.name,
                     capitalizedName, field.type, field.name, field.name, field.name);
    }

    private String generateFieldDeclarations(List<FieldInfo> fields) {
        if (fields.isEmpty()) {
            return """
                    // Add your Model fields here
                    // Example:
                    // private String name;
                """;
        }

        StringBuilder sb = new StringBuilder();
        for (FieldInfo field : fields) {
            sb.append(String.format("    private %s %s;\n", field.type, field.name));
        }
        return sb.toString();
    }

    private String generateAllGettersSetters(List<FieldInfo> fields) {
        if (fields.isEmpty()) {
            return "    // Add getters and setters for your fields\n";
        }

        StringBuilder sb = new StringBuilder();
        for (FieldInfo field : fields) {
            sb.append(generateGetterSetter(field));
        }
        return sb.toString();
    }

    private Set<String> extractImportsForFields(Path entityPath, List<FieldInfo> fields) throws Exception {
        Set<String> imports = new HashSet<>();
        String content = Files.readString(entityPath);

        // Extract all import statements from the entity file
        Pattern importPattern = Pattern.compile("^import\\s+([^;]+);", Pattern.MULTILINE);
        Matcher importMatcher = importPattern.matcher(content);

        Set<String> allImports = new HashSet<>();
        while (importMatcher.find()) {
            String importStatement = importMatcher.group(1).trim();
            allImports.add(importStatement);
        }

        // For each field, check if we need any of the imports
        for (FieldInfo field : fields) {
            String fieldType = field.type;

            // Extract the base type(s) from potentially generic types
            // E.g., "List<String>" -> ["List", "String"], "Map<String, Date>" -> ["Map", "String", "Date"]
            Set<String> typeNames = extractTypeNames(fieldType);

            for (String typeName : typeNames) {
                // Skip primitive types and java.lang types (automatically imported)
                if (isPrimitive(typeName) || typeName.equals("String") || typeName.equals("Long") ||
                    typeName.equals("Integer") || typeName.equals("Boolean") || typeName.equals("Double") ||
                    typeName.equals("Float") || typeName.equals("Character") || typeName.equals("Byte") ||
                    typeName.equals("Short")) {
                    continue;
                }

                // Find the import for this type
                for (String importStmt : allImports) {
                    // Skip JPA/Jakarta imports
                    if (importStmt.startsWith("jakarta.persistence") ||
                        importStmt.startsWith("javax.persistence")) {
                        continue;
                    }

                    // Check if this import is for our type
                    if (importStmt.endsWith("." + typeName)) {
                        imports.add(importStmt);
                        break;
                    }
                }
            }
        }

        return imports;
    }

    private Set<String> extractTypeNames(String type) {
        Set<String> names = new HashSet<>();

        // Remove generic brackets and split by common delimiters
        // E.g., "List<Map<String, Date>>" -> "List Map String Date"
        String cleaned = type.replaceAll("[<>,\\[\\]]", " ");
        String[] parts = cleaned.split("\\s+");

        for (String part : parts) {
            if (!part.isEmpty()) {
                names.add(part.trim());
            }
        }

        return names;
    }

    private boolean isPrimitive(String type) {
        return type.equals("int") || type.equals("long") || type.equals("double") ||
               type.equals("float") || type.equals("boolean") || type.equals("char") ||
               type.equals("byte") || type.equals("short");
    }

    private String generateImports(Set<String> imports) {
        if (imports.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (String importStmt : imports) {
            sb.append("import ").append(importStmt).append(";\n");
        }
        return sb.toString();
    }

}
