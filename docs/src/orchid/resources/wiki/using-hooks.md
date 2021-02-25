# Usage

At its core, this project exposes a base hooks library, which can be used by itself, but requires a somewhat verbose, redundant API to use. To limit the overhead of using hooks, we also expose a Kotlin compiler plugin built with [Arrow Meta](https://meta.arrow-kt.io/), which provides a simple, type-driven DSL to enable consumers to create hooks. Since Kotlin compiler plugins aren't necessarily easy to configure, we've built a Gradle plugin and a Maven Kotlin plugin extension to configure a project to use hooks. See the module documentation for more information on how to use hooks in your project:

##### Modules

* [Hooks](/modules/hooks)
* [Kotlin Compiler Plugin](/modules/compiler-plugin)
* [Gradle Plugin](/modules/gradle-plugin)
* [Maven Kotlin Plugin Extension](/modules/maven-plugin)
