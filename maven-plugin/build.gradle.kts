val compilerPlugin: Configuration by configurations.creating

dependencies {
    implementation(libs.kotlin.maven)
    implementation(libs.ksp.maven)
    compilerPlugin(project(":compiler-plugin"))
}

tasks {
    jar {
        dependsOn(":compiler-plugin:jar")
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
