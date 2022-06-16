package com.intuit.hooks.plugin.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer
import java.util.Properties

/** Wrap KSP plugin and provide Gradle extension for Hooks processor options */
public class HooksGradlePlugin : Plugin<Project> {

    private val properties by lazy {
        Properties().apply {
            HooksGradlePlugin::class.java.classLoader.getResourceAsStream("version.properties").let(::load)
        }
    }

    private val hooksVersion by lazy {
        properties["version"] as String
    }

    private fun Project.addDependency(configuration: String, dependencyNotation: String) = configurations
        .getByName(configuration).dependencies.add(
            dependencies.create(dependencyNotation)
        )

    override fun apply(project: Project): Unit = with(project) {
        extensions.create(
            "hooks",
            HooksGradleExtension::class.java
        )

        if (!pluginManager.hasPlugin("com.google.devtools.ksp"))
            pluginManager.apply("com.google.devtools.ksp")

        addDependency("api", "com.intuit.hooks:hooks:$hooksVersion")
        addDependency("ksp", "com.intuit.hooks:compiler-plugin:$hooksVersion")

        // TODO: Maybe apply to Kotlin plugin to be compatible with MPP
        plugins.withType(JavaPlugin::class.java) { _ ->
            val sourceSets = extensions.getByType(SourceSetContainer::class.java)
            sourceSets.forEach {
                it.java.srcDir(buildDir.resolve("generated/ksp/${it.name}/kotlin"))
            }
        }
    }
}
