import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.properties.Properties
import org.jetbrains.kotlin.konan.properties.saveToFile

plugins {
    id("com.gradle.plugin-publish") version "1.0.0-rc-3"
}

gradlePlugin {
    plugins {
        create("HooksGradlePlugin") {
            id = "com.intuit.hooks"
            implementationClass = "com.intuit.hooks.plugin.gradle.HooksGradlePlugin"
            displayName = "Gradle Hooks plugin"
        }
    }

    testSourceSets(sourceSets.test.get())
}

pluginBundle {
    website = "https://intuit.github.io/hooks/"
    vcsUrl = "https://github.com/intuit/hooks"
    description = "Gradle wrapper of the Kotlin compiler companion to the Intuit hooks module"
    tags = listOf("plugins", "hooks", "ksp", "codegen")
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.ksp.gradle)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.testing)
}

kotlin {
    explicitApi()
}

tasks {
    val createProperties by creating {
        dependsOn(processResources)

        doLast {
            Properties().apply {
                set("version", project.version.toString())
            }.saveToFile(File("$buildDir/resources/main/version.properties"))
        }
    }

    classes {
        dependsOn(createProperties)
    }

    test {
        dependsOn(":hooks:publishToMavenLocal", ":compiler-plugin:publishToMavenLocal")
    }
}
