package com.intuit.hooks.plugin.gradle

import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.SourceSetContainer
import java.util.*

/** Wrap KSP plugin and provide Gradle extension for Hooks processor options */
public class HooksGradlePlugin : Plugin<Project> {

    private val properties by lazy {
        val properties = Properties()
        HooksGradlePlugin::class.java.classLoader.getResourceAsStream("version.properties").let(properties::load)
        properties
    }

    private val version by lazy {
        properties["version"] as String
    }

    private fun Project.addDependency(configuration: String, dependencyNotation: String) = configurations
        .getByName(configuration).dependencies.add(
            dependencies.create(dependencyNotation)
        )

    override fun apply(target: Project) {
        val hooksExtension = target.extensions.create(
            "hooks",
            HooksGradleExtension::class.java
        )

        if (!target.pluginManager.hasPlugin("com.google.devtools.ksp"))
            target.pluginManager.apply("com.google.devtools.ksp")

        target.extensions.configure<KspExtension>("ksp") { ksp ->
            hooksExtension.generatedSrcOutputDir?.let { generatedSrcOutputDir ->
                ksp.arg("generatedSrcOutputDir", generatedSrcOutputDir)
            }
        }

        target.addDependency("api", "com.intuit.hooks:hooks:$version")
        target.addDependency("ksp", "com.intuit.hooks:compiler-plugin:$version")

        // TODO: Maybe apply to Kotlin plugin to be compatible with MPP
        target.plugins.withType(JavaPlugin::class.java) { _ ->
            val sourceSets = target.extensions.getByType(SourceSetContainer::class.java)
            sourceSets.forEach {
                it.java.srcDir(target.buildDir.resolve(hooksExtension.generatedSrcOutputDir ?: "generated/ksp/${it.name}/kotlin"))
            }
        }
    }
}
