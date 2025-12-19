package com.edusoftwerks.gwtboot.cli;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Command(
        name = "entity",
        description = "Create a JPA entity and repository from an existing model POJO in shared/models",
        mixinStandardHelpOptions = true
)
public class EntityCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "The model name (must match a model POJO in shared/models)")
    private String modelName;

    @Override
    public Integer call() throws Exception {
        if (modelName == null || modelName.trim().isEmpty()) {
            Console.error("Entity name is required");
            Console.println("Usage: gwt-boot entity <entity-name>");
            Console.println("Example: gwt-boot entity User");
            return 1;
        }

        // Capitalize entity name for class names
        final String modelClass = Utils.capitalize(modelName);

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
        Console.info("Entity Generator");
        Console.info("===================================");
        Console.println("");
        Console.println("Entity name: " + modelClass);
        Console.println("Package: " + packageName);
        Console.println("");

        // Check if model exists in shared/models
        Path sharedModelsDir = javaBase.resolve("shared/models");
        Path modelFile = sharedModelsDir.resolve(modelClass + ".java");

        List<FieldInfo> fields = new ArrayList<>();

        if (Files.exists(modelFile)) {
            Console.info("Found Model: " + modelFile);
            // Read and parse the Model

            String modelContent = Files.readString(modelFile);
            fields = parseFields(modelContent);
        }

        Path persistenceDir = javaBase.resolve("persistence");
        // Define entity and repository paths
        Path entitiesDir = persistenceDir.resolve("entities");
        Path repositoriesDir = persistenceDir.resolve("repositories");

        Path entityFile = entitiesDir.resolve(modelClass + "Entity.java");
        Path repositoryFile = repositoriesDir.resolve(modelClass + "Repository.java");

        // Check for existing files
        List<Path> existingFiles = new ArrayList<>();
        if (Files.exists(entityFile)) existingFiles.add(entityFile);
        if (Files.exists(repositoryFile)) existingFiles.add(repositoryFile);

        if (!existingFiles.isEmpty()) {
            Console.warning("WARNING: The following files will be overwritten:");
            for (Path file : existingFiles) {
                Console.println("  - " + file);
            }
            Console.println("");

            if (!InputReader.confirm("Continue with generation?", false)) {
                Console.warning("Entity generation cancelled.");
                return 1;
            }
            Console.println("");
        }

        // Create directories
        Console.info("Creating package structure...");
        Files.createDirectories(persistenceDir);
        Files.createDirectories(entitiesDir);
        Files.createDirectories(repositoriesDir);

        // Generate entity class
        Console.info("Creating " + modelClass + "Entity.java...");
        String entityContent = generateEntity(packageName, modelClass, fields);
        Files.writeString(entityFile, entityContent);

        // Generate repository
        Console.info("Creating " + modelClass + "Repository.java...");
        String repositoryContent = generateRepository(packageName, modelClass);
        Files.writeString(repositoryFile, repositoryContent);

        Console.println("");
        Console.success("===================================");
        Console.success("Entity created successfully!");
        Console.success("===================================");
        Console.println("");
        Console.println("Created:");
        Console.println("  - " + entityFile + " (JPA Entity)");
        Console.println("  - " + repositoryFile + " (Spring Data Repository)");
        Console.println("");
        Console.println("Next steps:");
        Console.println("  1. Review the generated entity and adjust column definitions if needed");
        Console.println("  2. Add any additional JPA annotations (@OneToMany, @ManyToOne, etc.)");
        Console.println("  3. Add custom query methods to " + modelClass + "Repository.java if needed");
        Console.println("");

        return 0;
    }

    private List<FieldInfo> parseFields(String content) {
        List<FieldInfo> fields = new ArrayList<>();

        // Pattern to match field declarations: private Type name;
        // This handles generic types like List<String>, arrays, etc.
        Pattern fieldPattern = Pattern.compile(
                "^\\s*private\\s+([\\w<>\\[\\],\\s]+?)\\s+(\\w+)\\s*;",
                Pattern.MULTILINE
        );

        Matcher matcher = fieldPattern.matcher(content);
        while (matcher.find()) {
            String type = matcher.group(1).trim();
            String name = matcher.group(2).trim();

            // Skip serialVersionUID and other static fields
            if (!name.equals("serialVersionUID")) {
                FieldInfo field = new FieldInfo(type, name);
                // Transform model types to entity types
                field.entityType = transformTypeToEntity(type);
                fields.add(field);
            }
        }

        return fields;
    }

    private String generateEntity(String packageName, String modelClass, List<FieldInfo> fields) {
        StringBuilder sb = new StringBuilder();

        boolean hasFields = !fields.isEmpty();

        sb.append("package ").append(packageName).append(".persistence.entities;\n\n");
        sb.append("import jakarta.persistence.*;\n");

        if (hasFields) {

            sb.append("""
                    import org.mapstruct.Mapper;
                    import org.mapstruct.MappingConstants;
                    import org.mapstruct.factory.Mappers;
                    """);

            boolean hasDate = fields.stream().anyMatch(f -> f.type.contains("Date"));
            boolean hasLocalDate = fields.stream().anyMatch(f -> f.type.contains("LocalDate") || f.type.contains("LocalDateTime"));
            boolean hasList = fields.stream().anyMatch(f -> f.entityType.contains("List"));
            boolean hasSet = fields.stream().anyMatch(f -> f.entityType.contains("Set"));
            boolean hasMap = fields.stream().anyMatch(f -> f.entityType.contains("Map"));

            if (hasDate) {
                sb.append("import java.util.Date;\n");
            }
            if (hasLocalDate) {
                sb.append("import java.time.*;\n");
            }
            if (hasList || hasSet || hasMap) {
                sb.append("import java.util.*;\n");
            }

            sb.append(String.format("\nimport %s.shared.models.%s;\n", packageName, modelClass));

        }
        // Add any necessary imports based on field types
        sb.append("\n");

        sb.append("\n");
        sb.append("@Entity\n");
        sb.append(String.format("@Table(name = \"%s\")\n", Utils.toSnakeCase(modelClass)));
        sb.append("public class ").append(modelClass).append("Entity {\n\n");

        // Add ID field
        sb.append("    @Id\n");
        sb.append("    @GeneratedValue(strategy = GenerationType.IDENTITY)\n");
        sb.append("    private Long id;\n\n");

        // Add fields from model
        for (FieldInfo field : fields) {
            if (!field.name.equals("id")) {
                // Check if this is a collection of entities (OneToMany relationship)
                if (isCollectionOfEntities(field.entityType)) {
                    sb.append("    @OneToMany\n");
                } else if (isSingleEntity(field.entityType)) {
                    // Single entity reference (ManyToOne relationship)
                    sb.append("    @ManyToOne\n");
                } else {
                    // Regular column
                    sb.append("    @Column\n");
                }
                sb.append("    private ").append(field.entityType).append(" ").append(field.name).append(";\n\n");
            }

        }

        // Add default constructor
        sb.append("    public ").append(modelClass).append("Entity() {\n");
        sb.append("    }\n\n");

        // Add ID getter
        sb.append("    public Long getId() {\n");
        sb.append("        return id;\n");
        sb.append("    }\n\n");

        // Add getters and setters for all fields
        for (FieldInfo field : fields) {
            if (!field.name.equals("id")) {
                String capitalizedName = Utils.capitalize(field.name);

                // Getter
                sb.append("    public ").append(field.entityType).append(" get").append(capitalizedName).append("() {\n");
                sb.append("        return ").append(field.name).append(";\n");
                sb.append("    }\n\n");

                // Setter
                sb.append("    public void set").append(capitalizedName).append("(").append(field.entityType).append(" ").append(field.name).append(") {\n");
                sb.append("        this.").append(field.name).append(" = ").append(field.name).append(";\n");
                sb.append("    }\n\n");
            }
        }

        sb.append("\n\n");
        if (hasFields) {
            sb.append(String.format("""
                        @Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
                        interface %1$sMapper {
                    
                            %1$sMapper INSTANCE = Mappers.getMapper(%1$sMapper.class);
                    
                            %1$s to(%1$sEntity entity);
                    
                            %1$sEntity from(%1$s model);
                    
                            // MapStruct will automatically generate implementations for these methods
                            // Add custom mappings if field names differ between entity and model
                            // Example:
                            // @Mapping(source = "entityField", target = "modelField")
                            // %1$s toCustom(%sEntity entity);
                    
                        }
                    
                        @Transient
                        public %1$s to%1$s() {
                            return %1$sMapper.INSTANCE.to(this);
                        }
                    
                        @Transient
                        public static %1$sEntity from%1$s(%1$s model) {
                            return %1$sMapper.INSTANCE.from(model);
                        }
                    """, modelClass));
        }
        sb.append("\n}\n");

        return sb.toString();
    }

    private String generateRepository(String packageName, String entityClass) {
        return String.format("""
                package %s.persistence.repositories;
                
                import %s.persistence.entities.%sEntity;
                import org.springframework.data.jpa.repository.JpaRepository;
                import org.springframework.stereotype.Repository;
                
                @Repository
                public interface %sRepository extends JpaRepository<%sEntity, Long> {
                
                    // Add custom query methods here
                    // Example:
                    // List<%sEntity> findByName(String name);
                
                }
                """, packageName, packageName, entityClass, entityClass, entityClass, entityClass);
    }

    private String transformTypeToEntity(String type) {
        // Handle primitives and common Java types that don't need transformation
        if (isPrimitiveOrStandardType(type)) {
            return type;
        }

        // Transform model types to entity types in generics
        // e.g., List<User> -> List<UserEntity>, Map<String, User> -> Map<String, UserEntity>
        Pattern genericPattern = Pattern.compile("([A-Z]\\w+)(?![a-z])");
        Matcher matcher = genericPattern.matcher(type);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String className = matcher.group(1);
            // Only transform if it looks like a custom model class
            if (isLikelyModelClass(className)) {
                matcher.appendReplacement(result, className + "Entity");
            } else {
                matcher.appendReplacement(result, className);
            }
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private boolean isPrimitiveOrStandardType(String type) {
        // Primitives
        if (type.matches("(int|long|short|byte|char|float|double|boolean)")) {
            return true;
        }

        // Boxed primitives and common Java types
        String baseType = type.replaceAll("<.*>", "").trim();
        return baseType.matches("(Integer|Long|Short|Byte|Character|Float|Double|Boolean|String|" +
                "BigDecimal|BigInteger|Date|LocalDate|LocalDateTime|LocalTime|Instant|UUID)");
    }

    private boolean isLikelyModelClass(String className) {
        // Skip common collection and standard Java classes
        if (className.matches("(List|Set|Map|Collection|ArrayList|HashSet|HashMap|" +
                "LinkedList|LinkedHashSet|LinkedHashMap|TreeSet|TreeMap|Optional|" +
                "String|Integer|Long|Short|Byte|Character|Float|Double|Boolean|" +
                "BigDecimal|BigInteger|Date|LocalDate|LocalDateTime|LocalTime|Instant|UUID)")) {
            return false;
        }

        // If it starts with uppercase and isn't in the exclusion list, it's likely a model
        return Character.isUpperCase(className.charAt(0));
    }

    private boolean isCollectionOfEntities(String type) {
        // Check if the type is a collection (List, Set, Collection) containing an Entity
        // e.g., List<UserEntity>, Set<OrderEntity>, Collection<ProductEntity>
        Pattern collectionPattern = Pattern.compile("(List|Set|Collection|ArrayList|HashSet)<([A-Z]\\w+Entity)>");
        return collectionPattern.matcher(type).find();
    }

    private boolean isSingleEntity(String type) {
        // Check if the type is a single entity reference (not a collection)
        // e.g., UserEntity, OrderEntity
        // Must end with "Entity" and not be part of a collection
        if (type.matches("[A-Z]\\w+Entity")) {
            // Make sure it's not inside a collection
            return !type.contains("<") && !type.contains(">");
        }
        return false;
    }

    private static class FieldInfo {
        String type;
        String entityType;
        String name;

        FieldInfo(String type, String name) {
            this.type = type;
            this.name = name;
            this.entityType = type; // Will be transformed later
        }
    }
}
