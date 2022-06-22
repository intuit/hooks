# Usage

At its core, this project exposes a base hooks library, which can be used by itself, but requires a somewhat verbose, redundant API to use. To limit the overhead of using hooks, we also expose a Kotlin symbol processor built with the [KSP API](https://kotlinlang.org/docs/ksp-overview.html), which provides a simple, type-driven DSL to enable consumers to create hooks. Kotlin symbol processors are relatively easy to integrate into Gradle projects, but to limit the configuration burden, we've built a Gradle plugin and a Maven Kotlin plugin extension to configure a project to use hooks. See the module documentation for more information on how to use hooks in your project:

##### Modules

* [Hooks](/hooks/modules/hooks)
* [Processor](/hooks/modules/processor)
* [Gradle Plugin](/hooks/modules/gradle-plugin)
* [Maven Kotlin Plugin Extension](/hooks/modules/maven-plugin)
