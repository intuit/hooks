val pluginDependency: Configuration by configurations.creating {
    configurations.compileClasspath.get().extendsFrom(this)
}

dependencies {
    implementation(kotlin("maven-plugin"))
    pluginDependency(project(":compiler-plugin"))
}

tasks {
    jar {
        fromConfiguration(pluginDependency)
    }
}
