package com.intuit.hooks.plugin.gradle

import org.gradle.api.Project
import org.gradle.api.artifacts.DependencyResolutionListener
import org.gradle.api.artifacts.ResolvableDependencies
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import java.nio.file.Paths
import java.util.*

/** Bridge between compiler-plugin and gradle plugin */
public class HooksGradlePlugin : KotlinCompilerPluginSupportPlugin {

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

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean = kotlinCompilation
        .target.project.plugins.hasPlugin(HooksGradlePlugin::class.java)

    override fun getCompilerPluginId(): String = "arrow.meta.plugin.compiler"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        "com.intuit.hooks",
        "compiler-plugin",
        version
    )

    override fun apply(target: Project) {
        target.extensions.create(
            "hooks",
            HooksGradleExtension::class.java
        )

        target.gradle.addListener(
            object : DependencyResolutionListener {
                override fun beforeResolve(dependencies: ResolvableDependencies) {
                    target.addDependency("api", "com.intuit.hooks:hooks:$version")
                    target.gradle.removeListener(this)
                }

                override fun afterResolve(dependencies: ResolvableDependencies) = Unit
            }
        )
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> = kotlinCompilation.target.project.run {
        val extension = extensions.findByType(HooksGradleExtension::class.java)
            ?: HooksGradleExtension()

        // do validations here
        val generatedSrcOutputDir = extension.generatedSrcOutputDir
            ?: buildDir.absolutePath

        // aggregate subplugin options
        val generated = SubpluginOption("generatedSrcOutputDir", generatedSrcOutputDir)

        // add generatedSrcOutputDir to default source set
        plugins.withType(JavaPlugin::class.java) { javaPlugin ->
            val sourceSets = extensions.getByType(SourceSetContainer::class.java)
            val main = sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
            main.java.srcDir(Paths.get(generatedSrcOutputDir, "generated", "source", "kapt", "main"))
        }

        val cleanGenerated = tasks.findByPath("cleanGenerated") ?: tasks.register("cleanGenerated") {
            it.group = "build"
            delete(generatedSrcOutputDir)
        }

        kotlinCompilation.compileKotlinTask.dependsOn(cleanGenerated)

        provider {
            listOf(generated)
        }
    }
}
