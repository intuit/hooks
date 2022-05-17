package com.intuit.hooks.plugin.maven

import com.dyescape.ksp.maven.KotlinSymbolProcessingMavenPluginExtension
import org.apache.maven.plugin.MojoExecution
import org.apache.maven.project.MavenProject
import org.apache.maven.repository.RepositorySystem
import org.codehaus.plexus.component.annotations.Component
import org.codehaus.plexus.component.annotations.Requirement
import org.codehaus.plexus.logging.Logger
import org.jetbrains.kotlin.maven.KotlinMavenPluginExtension
import org.jetbrains.kotlin.maven.PluginOption

/** Slim wrapper of [KotlinSymbolProcessingMavenPluginExtension] to apply additional plugin params */
@Component(role = KotlinMavenPluginExtension::class, hint = "hooks")
public class HooksMavenPlugin(
    private val delegate: KotlinSymbolProcessingMavenPluginExtension = KotlinSymbolProcessingMavenPluginExtension()
) : KotlinMavenPluginExtension by delegate {

    @Requirement
    public lateinit var system: RepositorySystem

    @Requirement
    public lateinit var logger: Logger

    override fun getPluginOptions(project: MavenProject, execution: MojoExecution): List<PluginOption> {
        delegate.system = system
        val options = delegate.getPluginOptions(project, execution)
        // TODO: call into delegate to get gen params to combine with hooks specific params
        return options
    }
}
