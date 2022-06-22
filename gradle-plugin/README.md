# Gradle Plugin

> **Warning**
> 
> The Gradle plugin automatically bundles a specific version of the KSP plugin, which is tied to a specific version of Kotlin (can be found [here](./settings.gradle.kts#19)). This means the Gradle plugin is only compatible with projects that use that specific Kotlin version. At some point, this module will be upgraded to publish in accordance to the KSP/Kotlin version it bundles.

Applying the hooks Gradle plugin automatically adds the appropriate dependencies to your project, configures the generated source directory, and registers the KSP plugin.

### Installation

Add the following to your modules `build.gradle(.kts)`:

```kotlin
// build.gradle(.kts)
plugins {
    // other plugins

    // apply hooks gradle plugin
    id("com.intuit.hooks") version "$HOOKS_VERSION"
}
```
