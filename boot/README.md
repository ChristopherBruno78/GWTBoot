# GWT Boot

A modern Maven archetype and CLI tool for creating GWT (Google Web Toolkit) and Spring Boot integrated projects.

## Installation

### Install the GWT Boot CLI

```bash
# Clone or download the gwt-boot archetype
cd gwt-boot-archetype/cli-tool

# Run the install script
./install.sh
```

This will:
- Install the archetype to your local Maven repository
- Copy `gwt-boot` to `/usr/local/bin` (requires sudo)

Verify installation:
```bash
gwt-boot version
```

## Quick Start

Generate a new GWT Boot project:

```bash
gwt-boot app myapp
```

Or run interactively:

```bash
gwt-boot app
```

The CLI will prompt you for:
- **groupId** - Your organization identifier (e.g., `com.mycompany`)
- **artifactId** - Your project name (e.g., `myapp`, or provided as argument)
- **version** - Project version (default: `0.0.1-SNAPSHOT`)
- **package** - Base package name (default: `groupId.artifactId`)

Open IntellJ and start the Run configuration.

Your application will be available at:
- **Spring Boot**: http://localhost:8080
- **GWT CodeServer**: http://localhost:9876

## GWT Boot CLI Commands

```bash
# Project Generation
gwt-boot app [appName]             # Create a new GWT Boot project

# Code Generation
gwt-boot activity <name>           # Generate a new activity module
gwt-boot service <name>            # Generate a new GWT RPC service
gwt-boot component <name>          # Generate a new UI component

# Development & Build
gwt-boot dev [-m <mb>]             # Launch GWT CodeServer and Spring Boot
gwt-boot jar [-w <workers>]        # Build production JAR with compiled GWT

# Help & Information
gwt-boot help                      # Show all commands
gwt-boot version                   # Show version information
```

## Project Features

Generated projects include:

### **Backend (Spring Boot 3.5.4)**
- Spring Data JPA
- Spring Security
- Spring Web & Thymeleaf
- H2 in-memory database
- Pre-configured modules (auth, document, testbank)

### **Frontend (GWT 2.12.3)**
- GWT User & Dev tools
- GWT Servlet for Jakarta EE
- Elemental2 DOM
- Mosaic MVP framework support

### **Development Tools**
- Maven wrapper for consistent builds
- GWT Boot CLI for rapid development
- IntelliJ IDEA configuration (GWT facet, Maven integration)
- Development profile for running both servers concurrently

### **Additional Libraries**
- PDFBox for PDF generation
- MapStruct for object mapping
- Apache Commons Codec

## Development Workflow

### Start Development Servers

Run both Spring Boot and GWT CodeServer together:

```bash
./mvnw -Pdev
```

## Creating Activities

Activities are self-contained modules with client, server, and shared code.

```bash
gwt-boot activity dashboard
```

This generates:

**Client-side (MVP Pattern)**
- `activities/dashboard/client/DashboardPresenter.java` - ViewPresenter
- `activities/dashboard/client/DashboardView.java` - View component
- `activities/dashboard/client/DashboardView.ui.xml` - UiBinder template

**Server-side**
- `activities/dashboard/DashboardController.java` - Spring Controller

**Shared**
- `activities/dashboard/shared/` - DTOs and shared models

**Configuration**
- `activities/dashboard/Dashboard.gwt.xml` - GWT module descriptor
- `templates/dashboard/index.html` - Thymeleaf template with GWT loader

**Access at**: `http://localhost:8080/dashboard`

### Activity Structure

Each activity follows the **Mosaic MVP pattern**:

- **Presenter** - Business logic and user interaction handling
- **View** - UI definition using UiBinder
- **Controller** - Spring Boot endpoint serving the template
- **Shared** - Code accessible by both client and server
- **GWT Module** - Inherits from main App module

## Creating GWT RPC Services

Generate type-safe client-server communication interfaces:

```bash
gwt-boot service UserAuth
```

This generates:

**Service Interface (Shared)**
```java
// shared/services/UserAuth/UserAuthService.java
@RemoteServiceRelativePath("../UserAuth/service")
public interface UserAuthService extends RemoteService {
    // Synchronous method signatures

    class Instance {
        private static final UserAuthServiceAsync INSTANCE = GWT.create(UserAuthService.class);

        public static UserAuthServiceAsync get() {
            return INSTANCE;
        }
    }
}
```

**Async Interface (Shared)**
```java
// shared/services/UserAuth/UserAuthServiceAsync.java
public interface UserAuthServiceAsync {
    // Async versions with AsyncCallback
}
```

**Service Implementation (Server)**
```java
// services/UserAuthServiceImpl.java
@WebServlet("/userauth/service")
@Service
public class UserAuthServiceImpl extends RemoteServiceServlet
    implements UserAuthService {
    // Spring Boot service implementation
}
```

**Service endpoint**: `/userauth/service`

### Using GWT RPC Services

1. Add methods to the service interface
2. Add corresponding async methods to the async interface
3. Implement methods in the service implementation
4. Get service instance: `UserAuthService.Instance.get()`

## Creating UI Components

Generate reusable UI components with UiBinder templates:

```bash
gwt-boot component button
```

This generates:

**Component Structure**
- `client/components/Button.java` - Component class
- `client/components/resources/button/Button.ui.xml` - UiBinder template
- `client/components/resources/button/style.css` - Component styles

**Component Class**
```java
package <package>.client.components;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class Button extends Composite {

    interface Resources extends ClientBundle {
        @Source("resources/button/style.css")
        Style style();
    }

    interface Style extends CssResource {
    }

    @UiTemplate("resources/button/Button.ui.xml")
    interface ButtonUiBinder extends UiBinder<Widget, Button> {}

    private static final ButtonUiBinder uiBinder = GWT.create(ButtonUiBinder.class);
    private static final Resources resources = GWT.create(Resources.class);

    public Button() {
        resources.style().ensureInjected();
        initWidget(uiBinder.createAndBindUi(this));
    }
}
```

The component uses:
- **ClientBundle** for CSS resource management with `@Source` annotation
- **UiTemplate** with `@Source` to reference the `.ui.xml` file
- Automatic CSS injection via `ensureInjected()`

The command automatically adds `<source path="client" />` to your main GWT module if not already present.

### Using Components

Import and use in your Views:
```java
import <package>.client.components.Button;

Button myButton = new Button();
```

## Project Structure

```
your-project/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── <package>/
│   │   │       ├── Application.java          # Spring Boot entry point
│   │   │       ├── <AppName>.gwt.xml         # Main GWT module
│   │   │       ├── activities/               # Activity modules
│   │   │       │   └── <activity>/
│   │   │       │       ├── client/           # GWT client code
│   │   │       │       ├── <Activity>Controller.java  # Spring controller
│   │   │       │       └── <Activity>.gwt.xml
│   │   │       ├── auth/                     # Authentication module
│   │   │       ├── client/                   # Shared client code
│   │   │       │   └── components/           # UI components
│   │   │       │       ├── <Component>.java
│   │   │       │       └── resources/
│   │   │       │           └── <component>/
│   │   │       │               ├── <Component>.ui.xml
│   │   │       │               └── style.css
│   │   │       ├── services/                 # RPC implementations
│   │   │       └── shared/
│   │   │           └── services/             # RPC service interfaces
│   │   │               └── <ServiceName>/    # Service package
│   │   │                   ├── <Service>Service.java
│   │   │                   └── <Service>ServiceAsync.java
│   │   └── resources/
│   │       ├── application.yml               # Spring Boot config
│   │       ├── static/                       # Static resources
│   │       └── templates/                    # Thymeleaf templates
│   └── test/
│       └── java/                             # Test files
├── pom.xml                                   # Maven configuration
├── mvnw / mvnw.cmd                           # Maven wrapper
└── README.md
```

## Configuration

### IntelliJ IDEA

Projects are pre-configured with:
- GWT facet (compiler parameters, SDK path, modules)
- Maven integration
- UTF-8 encoding
- Annotation processing
- JDK 21 compatibility

Simply open the project directory in IntelliJ IDEA to start developing.

### Application Settings

Edit `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: myapp
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
```

### Security Configuration

Customize authentication in the `auth` module's `SecurityConfig` class.

## Troubleshooting

### Common Issues

**Port conflicts**:
- Spring Boot (8080) or GWT CodeServer (9876) ports already in use
- Solution: Change ports in `application.yml`

**Servlet API conflicts**:
- Both `javax.servlet` (for GWT dev tools) and `jakarta.servlet` (for Spring Boot) are included
- This is expected and required for compatibility

**Line ending issues on Windows**:
- Scripts automatically fix CRLF → LF on generation
- If issues persist, run: `dos2unix gwt-boot` or use your IDE to fix line endings

### Getting Help

```bash
gwt-boot help
```

Visit the project repository for issues and documentation.

## License

This archetype is based on the TestMaker project structure.

## Version

GWT Boot CLI version 1.0.0
