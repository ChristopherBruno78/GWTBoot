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

        // Prompt for version (with default)
        String version = InputReader.readLineWithDefault(
            "Enter version (default: 0.0.1-SNAPSHOT): ",
            "0.0.1-SNAPSHOT"
        );

        // Prompt for package (with default derived from groupId.artifactId)
        String defaultPackage = groupId + "." + artifactId.toLowerCase();
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
            Console.println("");
            Console.info("Setting up IntelliJ IDEA configuration...");
            Console.println("");

            createIntellijConfig(artifactId, packageName);

            // Fix line endings and make scripts executable
            Path mvnwPath = Paths.get(artifactId, "mvnw");
            if (Files.exists(mvnwPath)) {
                //ProcessExecutor.fixLineEndings(mvnwPath.toString());
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
            Console.println("IntelliJ IDEA:");
            Console.println("  Open the project in IntelliJ IDEA");
            Console.println("  The GWT facet and Spring Boot are pre-configured");
            Console.println("");
        } else {
            Console.println("");
            Console.error("Project generation failed");
            return 1;
        }

        return 0;
    }

    private void createIntellijConfig(String artifactId, String packageName) throws IOException {
        Path ideaDir = Paths.get(artifactId, ".idea");
        Files.createDirectories(ideaDir);

        // Create .idea/.gitignore
        Files.writeString(ideaDir.resolve(".gitignore"),
            """
            # Default ignored files
            /shelf/
            /workspace.xmlIn
            # Editor-based HTTP Client requests
            /httpRequests/
            # Datasource local storage ignored files
            /dataSources/
            /dataSources.local.xml
            """
        );

        // Create .idea/compiler.xml
        Files.writeString(ideaDir.resolve("compiler.xml"),
            String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <project version="4">
              <component name="CompilerConfiguration">
                <annotationProcessing>
                  <profile name="Maven default annotation processors profile" enabled="true">
                    <sourceOutputDir name="target/generated-sources/annotations" />
                    <sourceTestOutputDir name="target/generated-test-sources/test-annotations" />
                    <outputRelativeToContentRoot value="true" />
                    <module name="%s" />
                  </profile>
                </annotationProcessing>
              </component>
              <component name="JavacSettings">
                <option name="ADDITIONAL_OPTIONS_OVERRIDE">
                  <module name="%s" options="-parameters" />
                </option>
              </component>
            </project>
            """, artifactId, artifactId)
        );

        // Create .idea/encodings.xml
        Files.writeString(ideaDir.resolve("encodings.xml"),
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <project version="4">
              <component name="Encoding">
                <file url="file://$PROJECT_DIR$/src/main/java" charset="UTF-8" />
              </component>
            </project>
            """
        );

        // Create .idea/jarRepositories.xml
        Files.writeString(ideaDir.resolve("jarRepositories.xml"),
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <project version="4">
              <component name="RemoteRepositoriesConfiguration">
                <remote-repository>
                  <option name="id" value="central" />
                  <option name="name" value="Central Repository" />
                  <option name="url" value="https://repo.maven.apache.org/maven2" />
                </remote-repository>
                <remote-repository>
                  <option name="id" value="central" />
                  <option name="name" value="Maven Central repository" />
                  <option name="url" value="https://repo1.maven.org/maven2" />
                </remote-repository>
                <remote-repository>
                  <option name="id" value="jboss.community" />
                  <option name="name" value="JBoss Community repository" />
                  <option name="url" value="https://repository.jboss.org/nexus/content/repositories/public/" />
                </remote-repository>
              </component>
            </project>
            """
        );

        // Create .idea/misc.xml
        Files.writeString(ideaDir.resolve("misc.xml"),
            """
            <?xml version="1.0" encoding="UTF-8"?>
            <project version="4">
              <component name="ExternalStorageConfigurationManager" enabled="true" />
              <component name="MavenProjectsManager">
                <option name="originalFiles">
                  <list>
                    <option value="$PROJECT_DIR$/pom.xml" />
                  </list>
                </option>
              </component>
              <component name="ProjectRootManager" version="2" languageLevel="JDK_21" default="true" project-jdk-name="azul-21" project-jdk-type="JavaSDK" />
            </project>
            """
        );

        // Create .idea/modules.xml
        Files.writeString(ideaDir.resolve("modules.xml"),
            String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <project version="4">
              <component name="ProjectModuleManager">
                <modules>
                  <module fileurl="file://$PROJECT_DIR$/%s.iml" filepath="$PROJECT_DIR$/%s.iml" />
                </modules>
              </component>
            </project>
            """, artifactId, artifactId)
        );

        // Create .idea/runConfigurations directory
        Path runConfigsDir = ideaDir.resolve("runConfigurations");
        Files.createDirectories(runConfigsDir);

        // Create Spring Boot run configuration
        Files.writeString(runConfigsDir.resolve("Application.xml"),
            String.format("""
            <component name="ProjectRunConfigurationManager">
              <configuration default="false" name="Application" type="SpringBootApplicationConfigurationType" factoryName="Spring Boot">
                <module name="%s" />
                <option name="SPRING_BOOT_MAIN_CLASS" value="%s.Application" />
                <method v="2">
                  <option name="Make" enabled="true" />
                </method>
              </configuration>
            </component>
            """, artifactId, packageName)
        );

        // Create GWT run configuration
        Files.writeString(runConfigsDir.resolve("GWT.xml"),
            String.format("""
            <component name="ProjectRunConfigurationManager">
              <configuration default="false" name="GWT" type="GWT.ConfigurationType" factoryName="GWT Configuration">
                <option name="VM_PARAMETERS" value="-Xmx2048m -Dgwt.persistentunitcachedir=target" />
                <option name="SHELL_PARAMETERS" value="-noserver -war target/classes/static" />
                <option name="USE_SUPER_DEV_MODE" value="true" />
                <option name="WORKING_DIRECTORY" value="$MODULE_DIR$" />
                <module name="%s" />
                <method v="2">
                  <option name="Make" enabled="true" />
                </method>
              </configuration>
            </component>
            """, artifactId)
        );

        // Create Compound run configuration (runs both)
        Files.writeString(runConfigsDir.resolve("Run.xml"),
            """
            <component name="ProjectRunConfigurationManager">
              <configuration default="false" name="Run" type="CompoundRunConfigurationType">
                <toRun name="GWT" type="GWT.ConfigurationType" />
                <toRun name="Application" type="SpringBootApplicationConfigurationType" />
                <method v="2" />
              </configuration>
            </component>
            """
        );

        // Create .iml file with GWT facet
        Files.writeString(Paths.get(artifactId, artifactId + ".iml"),
            String.format("""
            <?xml version="1.0" encoding="UTF-8"?>
            <module version="4">
              <component name="FacetManager">
                <facet type="gwt" name="GWT">
                  <configuration>
                    <setting name="compilerParameters" value="-sourceLevel 11" />
                    <setting name="compilerMaxHeapSize" value="2048" />
                    <setting name="gwtScriptOutputStyle" value="DETAILED" />
                    <setting name="gwtSdkUrl" value="file://$MAVEN_REPOSITORY$/org/gwtproject/gwt-dev/2.12.3" />
                    <setting name="gwtSdkType" value="maven" />
                    <packaging>
                      <module name="%s.App" path="/app" />
                    </packaging>
                  </configuration>
                </facet>
              </component>
            </module>
            """, packageName)
        );

        Console.info("IntelliJ IDEA configuration created.");
    }
}
