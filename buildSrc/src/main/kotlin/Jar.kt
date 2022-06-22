import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.CopySpec
import org.gradle.api.file.DuplicatesStrategy

fun Jar.fromConfiguration(configuration: NamedDomainObjectProvider<Configuration>, block: CopySpec.() -> Unit = {
    this.duplicatesStrategy = DuplicatesStrategy.INCLUDE
}) = fromConfiguration(configuration.get(), block)

fun Jar.fromConfiguration(configuration: Configuration, block: CopySpec.() -> Unit = {
    this.duplicatesStrategy = DuplicatesStrategy.INCLUDE
}) = from(configuration.map { if (it.isDirectory) it else project.zipTree(it) }, block)
