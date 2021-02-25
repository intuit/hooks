import groovy.lang.GroovyObject
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jfrog.gradle.plugin.artifactory.dsl.PublisherConfig
import org.jfrog.gradle.plugin.artifactory.task.ArtifactoryTask

allprojects {
    group = "com.intuit.hooks"

    repositories {
        jcenter()
        mavenLocal()
        mavenCentral()
        maven("https://plugins.gradle.org/m2/")
        maven("https://oss.jfrog.org/artifactory/oss-snapshot-local/")
    }
}

plugins {
    kotlin("jvm") apply false
    id("jacoco")

    id("com.jfrog.artifactory")
    id("net.researchgate.release")

    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.binary-compatibility-validator")

    id("org.jetbrains.dokka")
}

apiValidation {
    ignoredProjects.addAll(listOf("docs", "example-library", "example-application"))
}

release {
    failOnUpdateNeeded = false
    failOnPublishNeeded = false
    failOnSnapshotDependencies = false

    preTagCommitMessage = "[skip ci] pre tag commit: "
    tagCommitMessage = "[skip ci] creating tag: "
    newVersionCommitMessage = "[skip ci] new version commit: "
}

tasks {
    val build by creating {
        group = "build"
        afterEvaluate {
            subprojects.onEach { dependsOn(it.tasks.named("build")) }
        }
    }

    val publish by creating {
        group = "publishing"
    }

    val version by creating {
        doLast {
            println(version)
        }
    }

    dokkaHtmlMultiModule.configure {
        outputDirectory.set(rootDir.resolve("docs"))
    }
}

subprojects {
    apply {
        plugin("java-library")
        plugin("kotlin")
        plugin("jacoco")
        plugin("org.jlleitschuh.gradle.ktlint")
    }

    if (!name.contains("example")) {
        apply {
            plugin("maven-publish")
            plugin("com.jfrog.artifactory")
            plugin("org.jetbrains.dokka")
        }

        configure<KotlinJvmProjectExtension> {
            explicitApi()
        }

        artifactory {
            setContextUrl("https://artifact.intuit.com/artifactory")

            publish(
                delegateClosureOf<PublisherConfig> {
                    repository(
                        delegateClosureOf<GroovyObject> {
                            setProperty(
                                "repoKey",
                                if (version.toString().contains("SNAPSHOT")) "CG.PD.Intuit-Snapshots" else "CG.PD.Intuit-Releases"
                            )
                            setProperty("username", System.getenv("ARTIFACTORY_USERNAME"))
                            setProperty("password", System.getenv("ARTIFACTORY_PASSWORD"))
                            setProperty("maven", true)
                        }
                    )

                    defaults(
                        delegateClosureOf<ArtifactoryTask> {
                            publications("jar")
                        }
                    )
                }
            )
        }

        afterEvaluate {
            configure<PublishingExtension> {
                publications {
                    register<MavenPublication>("jar") {
                        from(components.getByName("java"))
                    }
                }
            }
        }

        tasks {
            named("publish") {
                dependsOn(artifactoryPublish)
            }
        }
    }

    ktlint {
        filter {
            exclude("**/*Impl.kt")
            exclude("**/example/**/*.kt")
        }
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        withSourcesJar()
    }

    tasks {
        val configure: KotlinCompile.() -> Unit = {
            kotlinOptions {
                val JVM_TARGET_VERSION: String by project
                jvmTarget = JVM_TARGET_VERSION
                freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
            }
        }

        named("compileKotlin", configure)

        named("compileTestKotlin", configure)

        named<Test>("test") {
            useJUnitPlatform()

            testLogging {
                events("passed", "skipped", "failed")
                showStackTraces = true
                exceptionFormat = TestExceptionFormat.FULL
            }

            finalizedBy(named("jacocoTestReport"))
        }
    }
}
