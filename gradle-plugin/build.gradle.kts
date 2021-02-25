import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.properties.Properties
import org.jetbrains.kotlin.konan.properties.saveToFile

plugins {
    `java-gradle-plugin`
}

gradlePlugin {
    plugins {
        create("HooksGradlePlugin") {
            id = "com.intuit.hooks"
            implementationClass = "com.intuit.hooks.plugin.gradle.HooksGradlePlugin"
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("gradle-plugin-api"))
}

tasks {
    val createProperties by creating {
        dependsOn(processResources)

        doLast {
            val ARROW_VERSION: String by project

            Properties().apply {
                set("version", project.version.toString())
                set("arrowVersion", ARROW_VERSION)
            }.saveToFile(File("$buildDir/resources/main/version.properties"))
        }
    }

    classes {
        dependsOn(createProperties)
    }
}
