import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.properties.Properties
import org.jetbrains.kotlin.konan.properties.saveToFile

plugins {
    `java-gradle-plugin`
    alias(libs.plugins.gradle.publish)
}

gradlePlugin {
    plugins {
        create("HooksGradlePlugin") {
            id = "com.intuit.hooks"
            implementationClass = "com.intuit.hooks.plugin.gradle.HooksGradlePlugin"
        }
    }

    testSourceSets(sourceSets.test.get())
}

pluginBundle {
    website = "https://intuit.github.io/hooks/"
    vcsUrl = "https://github.com/intuit/hooks"
    description = "Gradle wrapper of the Kotlin symbol processor companion to the Intuit hooks module"
    tags = listOf("plugins", "hooks")

    plugins {
        named("HooksGradlePlugin") {
            displayName = "Gradle Hooks plugin"
        }
    }
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
            val properties = "$buildDir/resources/main/com/intuit/hooks/plugin/gradle"
                .let(::File)
                .apply(File::mkdirs)
                .child("version.properties")

            Properties().apply {
                set("version", project.version.toString())
            }.saveToFile(properties)
        }
    }

    classes {
        dependsOn(createProperties)
    }

    test {
        // TODO: Testing migration required the deps to be pulled from somewhere
        //       Would be nice if they could just use the local built JARs
        dependsOn(":hooks:publishToMavenLocal", ":processor:publishToMavenLocal")
    }
}
