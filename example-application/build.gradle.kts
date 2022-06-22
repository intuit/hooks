plugins {
    application
}

// Resolvable configuration to help configure dependency resolution for the jar task
val projectImplementation: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)
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
