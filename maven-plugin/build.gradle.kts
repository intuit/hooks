val compilerPlugin: Configuration by configurations.creating

dependencies {
    implementation(libs.kotlin.maven)
    implementation("com.dyescape", "kotlin-maven-symbol-processing", "1.3")
    compilerPlugin(project(":compiler-plugin"))
}

tasks {
    jar {
        dependsOn(project(":compiler-plugin").getTasksByName("jar", false))
        fromConfiguration(compilerPlugin) {
            this.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }

        from(
            configurations.compileClasspath.get().filter { dependency ->
                dependency.absolutePath.contains("kotlin-maven-symbol-processing")
            }.map(::zipTree)
        ) {
            this.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        }
    }
}
