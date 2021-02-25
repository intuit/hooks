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

Until this plugin is officially published, you will need to instruct Gradle where to find the plugin:
```kotlin
// settings.gradle.kts
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://artifact.intuit.com/artifactory/CG.PD.Intuit-Releases")
    }
    
    resolutionStrategy {
        eachPlugin {
            when (val id = requested.id.id) {
                "com.intuit.hooks" -> useModule("$id:gradle-plugin:${requested.version}")
            }
        }
    }
}
```
