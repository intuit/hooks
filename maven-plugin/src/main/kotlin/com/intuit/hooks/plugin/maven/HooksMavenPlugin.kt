package com.intuit.hooks.plugin.maven

import org.apache.maven.plugin.MojoExecution
import org.apache.maven.project.MavenProject
import org.codehaus.plexus.component.annotations.Component
import org.codehaus.plexus.component.annotations.Requirement
import org.codehaus.plexus.logging.Logger
import org.jetbrains.kotlin.maven.KotlinMavenPluginExtension
import org.jetbrains.kotlin.maven.PluginOption

/** Bridge between hooks-plugin and Kotlin maven plugin */
@Component(role = KotlinMavenPluginExtension::class, hint = "hooks")
public class HooksMavenPlugin : KotlinMavenPluginExtension {

    @Requirement
    public lateinit var logger: Logger

    override fun isApplicable(project: MavenProject, mojo: MojoExecution): Boolean = true

    override fun getCompilerPluginId(): String = "arrow.meta.plugin.compiler"

    override fun getPluginOptions(project: MavenProject, mojo: MojoExecution): List<PluginOption> {
        logger.debug("Loaded Maven plugin ${javaClass.name}")

//         TODO: Add CLI processor for hook specific plugin option
        val generatedSrcOutputDir = null // extension.generatedSrcOutputDir
            ?: project.build.directory

//        TODO: This doesn't work, but it'd be real nice if it did
//        project.addCompileSourceRoot("$generatedSrcOutputDir/generated/source/kapt/main")
//        project.dependencies.add(Dependency().apply {
//            groupId = "io.arrow-kt"
//            artifactId = "arrow-annotations"
//            version = "0.11.0"
//            scope = "provided"
//        })

        return listOf(
            PluginOption("plugin", "arrow.meta.plugin.compiler", "generatedSrcOutputDir", generatedSrcOutputDir)
        )
    }
}
