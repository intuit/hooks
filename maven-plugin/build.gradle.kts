val ksp: Configuration by configurations.creating

dependencies {
    implementation(libs.kotlin.maven)
    implementation(libs.ksp.maven)
    ksp(project(":processor"))
}

tasks {
    jar {
        dependsOn(":processor:jar")
        fromConfiguration(ksp) {
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
