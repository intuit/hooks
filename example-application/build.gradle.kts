plugins {
    application
}

val COROUTINES_VERSION: String by project

// Resolvable configuration to help configure dependency resolution for the jar task
val projectImplementation: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", COROUTINES_VERSION)

    projectImplementation(project(":example-library"))
}

application {
    mainClass.set("com.intuit.hooks.example.application.MainKt")
}

tasks {
    jar {
        dependsOn(projectImplementation)

        manifest {
            attributes["Main-Class"] = "com.intuit.hooks.example.application.MainKt"
        }

        fromConfiguration(configurations.runtimeClasspath)
    }
}
