import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.CopySpec

fun Jar.fromConfiguration(configuration: NamedDomainObjectProvider<Configuration>, block: CopySpec.() -> Unit = {}) =
    fromConfiguration(configuration.get(), block)

fun Jar.fromConfiguration(configuration: Configuration, block: CopySpec.() -> Unit = {}) =
    from(configuration.map { if (it.isDirectory) it else getProject().zipTree(it) }, block)
