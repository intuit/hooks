# Gradle Plugin

Applying the hooks Gradle plugin automatically adds the appropriate dependencies to your project, configures the generated source directory, and registers the Kotlin compiler plugin.

### Installation

Add the following to your modules `build.gradle(.kts)`:

```kotlin
// build.gradle(.kts)
plugins {
    // other plugins

    // apply hooks gradle plugin
    id("com.intuit.hooks") version "$HOOKS_VERSION"
}

// Optional - Hooks extension configuration block
hooks {
    // Optional - configure output directory for generated code
    // Default - "${buildDir.absolutePath}/generated/source/kapt/main"
    generatedSrcOutputDir = "$buildDir/custom/generated/code"
}
```
