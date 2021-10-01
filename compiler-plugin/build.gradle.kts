import java.nio.file.Paths

dependencies {
    val ARROW_VERSION: String by project
    val ARROW_META_VERSION: String by project

    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable")
    compileOnly("io.arrow-kt:arrow-meta:$ARROW_META_VERSION")
    compileOnly("io.arrow-kt:arrow-core:$ARROW_VERSION")
    compileOnly("io.arrow-kt:arrow-annotations:$ARROW_VERSION")

    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter", "junit-jupiter")
    testImplementation("io.arrow-kt:meta-test:$ARROW_META_VERSION")

    testImplementation(project(":hooks"))
    testImplementation("io.arrow-kt:arrow-meta:$ARROW_META_VERSION")
    testRuntimeOnly("io.arrow-kt:arrow-meta-prelude:$ARROW_META_VERSION")
    testRuntimeOnly("io.arrow-kt:arrow-core:$ARROW_VERSION") {
        exclude("org.jetbrains.kotlin")
    }
}

tasks {
    jar {
        from(
            sourceSets.main.get().compileClasspath.filter {
                it.absolutePath.contains(Paths.get("arrow-kt").toString())
            }.map(::zipTree)
        )
    }
}
