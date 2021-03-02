rootProject.name = "hooks-project"
include(":hooks", ":compiler-plugin", ":gradle-plugin", ":maven-plugin", ":docs", ":example-library", ":example-application")
enableFeaturePreview("ONE_LOCKFILE_PER_PROJECT")

pluginManagement {
    val KOTLIN_VERSION: String by settings
    val RELEASE_PLUGIN_VERSION: String by settings
    val NEXUS_PUBLISH_PLUGIN_VERSION: String by settings
    val KTLINT_PLUGIN_VERSION: String by settings
    val VALIDATOR_VERSION: String by settings
    val DOKKA_VERSION: String by settings
    val ORCHID_VERSION: String by settings
    val KNIT_VERSION: String by settings

    plugins {
        kotlin("jvm") version KOTLIN_VERSION

        id("net.researchgate.release") version RELEASE_PLUGIN_VERSION
        id("io.github.gradle-nexus.publish-plugin") version NEXUS_PUBLISH_PLUGIN_VERSION

        id("org.jlleitschuh.gradle.ktlint") version KTLINT_PLUGIN_VERSION
        id("org.jetbrains.kotlinx.binary-compatibility-validator") version VALIDATOR_VERSION

        id("org.jetbrains.dokka") version DOKKA_VERSION
        id("com.eden.orchidPlugin") version ORCHID_VERSION
    }

    resolutionStrategy {
        eachPlugin {
            when (val id = requested.id.id) {
                "kotlinx-knit" -> useModule("org.jetbrains.kotlinx:$id:${requested.version ?: KNIT_VERSION}")
            }
        }
    }
}
