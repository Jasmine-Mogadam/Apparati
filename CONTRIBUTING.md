# Contributing

Thank you for your interest in contributing to this project!

## Development Environment Requirements

### Java Version

This project requires **Java 25** to run the Gradle build system. This is because the `RetroFuturaGradle` plugin (version 2.0.2) is compiled for Java 25.

If you use [SDKMAN!](https://sdkman.io/), you can install and use the correct version with:

```bash
sdk install java 25.0.2-tem
sdk use java 25.0.2-tem
```

### IDE Setup

- **IntelliJ IDEA:** Ensure that the Gradle JVM is set to Java 25. Go to `Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM` and select your Java 25 installation.
- I am slightly insane and use vscode for everything, so if you are a eclipse and intellij hater, feel free to use vscode instead B)
- **MinecraftDev Plugin:** It is recommended to use the [MinecraftDev Fork for RetroFuturaGradle](https://github.com/eigenraven/MinecraftDev/releases) for better Mixin support.

## Getting Started

1. Clone the repository.
2. Load the project into your IDE (linked via `build.gradle`).
3. Run `./gradlew build` to verify your environment is set up correctly.

## Coding Standards

Please follow the existing code style in the project. If you are adding new features, include relevant documentation and tests where appropriate.
