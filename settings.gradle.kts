rootProject.name = "hooks-project"
include(":hooks")//, ":compiler-plugin", ":gradle-plugin", ":maven-plugin", ":docs", ":example-library", ":example-application")
enableFeaturePreview("ONE_LOCKFILE_PER_PROJECT")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.6.21")
            version("ktlint", "0.45.2")
            version("arrow.core", "1.0.1") // 1.1.2
            version("ksp", "1.6.21-1.0.5")
            version("junit", "5.7.0")

            plugin("kotlin.jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")

            plugin("release", "net.researchgate.release").version("2.6.0")
            plugin("nexus", "io.github.gradle-nexus.publish-plugin").version("1.0.0")

            plugin("ktlint", "org.jlleitschuh.gradle.ktlint").version("10.3.0")
            plugin("api", "org.jetbrains.kotlinx.binary-compatibility-validator").version("0.9.0")

            plugin("knit", "kotlinx-knit").version("0.2.3")
            plugin("dokka", "org.jetbrains.dokka").versionRef("kotlin")
            plugin("orchid", "com.eden.orchidPlugin").version("0.21.1")

            library("kotlin.stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").withoutVersion()
            library("coroutines.core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version("1.6.1")

            // TODO: Swap to Kotlin testing library
            library("junit.bom", "org.junit", "junit-bom").version("5.7.0")
            library("junit.jupiter", "org.junit.jupiter", "junit-jupiter").withoutVersion()
            library("mockk", "io.mockk", "mockk").version("1.10.2")

            bundle("testing", listOf("junit.jupiter", "mockk"))
        }
    }
}

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (val id = requested.id.id) {
                "kotlinx-knit" -> useModule("org.jetbrains.kotlinx:$id:${requested.version!!}")
            }
        }
    }
}
