rootProject.name = "hooks-project"
include(
    ":hooks",
    ":processor",
    ":gradle-plugin",
    ":maven-plugin",
    ":docs",
    ":example-library",
    ":example-application",
)
enableFeaturePreview("ONE_LOCKFILE_PER_PROJECT")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            version("kotlin", "1.8.10")
            version("ktlint", "0.49.1")
            version("arrow", "1.2.0-RC")
            version("ksp", "1.8.10-1.0.9")
            version("poet", "1.12.0")
            version("junit", "5.7.0")
            version("knit", "0.4.0")
            version("orchid", "0.21.1")

            plugin("kotlin.jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("ksp", "com.google.devtools.ksp").versionRef("ksp")

            plugin("release", "net.researchgate.release").version("2.6.0")
            plugin("nexus", "io.github.gradle-nexus.publish-plugin").version("1.0.0")
            plugin("gradle.publish", "com.gradle.plugin-publish").version("0.13.0")

            plugin("ktlint", "org.jlleitschuh.gradle.ktlint").version("11.3.2")
            plugin("api", "org.jetbrains.kotlinx.binary-compatibility-validator").version("0.13.1")

            plugin("knit", "kotlinx-knit").versionRef("knit")
            plugin("dokka", "org.jetbrains.dokka").versionRef("kotlin")
            plugin("orchid", "com.eden.orchidPlugin").versionRef("orchid")

            // Kotlin
            library("kotlin.stdlib", "org.jetbrains.kotlin", "kotlin-stdlib").withoutVersion()
            library("kotlin.maven", "org.jetbrains.kotlin", "kotlin-maven-plugin").withoutVersion()
            library("kotlin.coroutines.core", "org.jetbrains.kotlinx", "kotlinx-coroutines-core").version("1.6.1")

            // KSP
            library("ksp.spa", "com.google.devtools.ksp", "symbol-processing-api").versionRef("ksp")
            library("ksp.poet", "com.squareup", "kotlinpoet-ksp").versionRef("poet")
            library("ksp.gradle", "com.google.devtools.ksp", "com.google.devtools.ksp.gradle.plugin").versionRef("ksp")
            library("ksp.maven", "com.dyescape", "kotlin-maven-symbol-processing").version("1.3")
            library("ktlint.core", "com.pinterest.ktlint", "ktlint-core").versionRef("ktlint")
            library("ktlint.ruleset.standard", "com.pinterest.ktlint", "ktlint-ruleset-standard").versionRef("ktlint")

            // Arrow
            library("arrow.core", "io.arrow-kt", "arrow-core").versionRef("arrow")

            // Docs
            library("orchid.core", "io.github.javaeden.orchid", "OrchidCore").versionRef("orchid")
            library("orchid.copper", "io.github.javaeden.orchid", "OrchidCopper").versionRef("orchid")

            library("orchid.plugins.docs", "io.github.javaeden.orchid", "OrchidDocs").versionRef("orchid")
            library("orchid.plugins.kotlindoc", "io.github.javaeden.orchid", "OrchidKotlindoc").versionRef("orchid")
            library("orchid.plugins.plugindocs", "io.github.javaeden.orchid", "OrchidPluginDocs").versionRef("orchid")
            library("orchid.plugins.github", "io.github.javaeden.orchid", "OrchidGithub").versionRef("orchid")
            library("orchid.plugins.changelog", "io.github.javaeden.orchid", "OrchidChangelog").versionRef("orchid")
            library("orchid.plugins.syntaxHighlighter", "io.github.javaeden.orchid", "OrchidSyntaxHighlighter").versionRef("orchid")
            library("orchid.plugins.snippets", "io.github.javaeden.orchid", "OrchidSnippets").versionRef("orchid")
            library("orchid.plugins.copper", "io.github.javaeden.orchid", "OrchidCopper").versionRef("orchid")
            library("orchid.plugins.wiki", "io.github.javaeden.orchid", "OrchidWiki").versionRef("orchid")

            bundle(
                "orchid.plugins",
                listOf(
                    "orchid.plugins.docs",
                    "orchid.plugins.kotlindoc",
                    "orchid.plugins.plugindocs",
                    "orchid.plugins.github",
                    "orchid.plugins.changelog",
                    "orchid.plugins.syntaxHighlighter",
                    "orchid.plugins.snippets",
                    "orchid.plugins.copper",
                    "orchid.plugins.wiki",
                ),
            )

            // Testing
            // TODO: Swap to Kotlin testing library
            library("junit.bom", "org.junit", "junit-bom").version("5.7.0")
            library("junit.jupiter", "org.junit.jupiter", "junit-jupiter").withoutVersion()
            library("mockk", "io.mockk", "mockk").version("1.10.2")
            library("ksp.testing", "com.github.tschuchortdev", "kotlin-compile-testing-ksp").version("1.5.0")
            library("knit.testing", "org.jetbrains.kotlinx", "kotlinx-knit-test").versionRef("knit")

            bundle("testing", listOf("junit.jupiter", "mockk"))
        }
    }
}

pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (val id = requested.id.id) {
                "kotlinx-knit" -> useModule("org.jetbrains.kotlinx:$id:${requireNotNull(requested.version)}")
            }
        }
    }
}
