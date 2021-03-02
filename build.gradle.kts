import AuthDelegate.Companion.auth
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

allprojects {
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

    id("net.researchgate.release")
    id("io.codearte.nexus-staging")

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

val sonatypeUsername by auth
val sonatypePassword by auth
val sonatypeStagingId by auth

nexusStaging {
    val group: String by project
    packageGroup = group
    stagingProfileId = sonatypeStagingId
    username = sonatypeUsername
    password = sonatypePassword
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
        dependsOn(":docs:orchidDeploy", ":gradle-plugin:publishPlugins")
//        named("closeAndReleaseRepository").get().dependsOn(this)
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

    if (!name.contains("example") && !name.contains("docs")) {
        apply {
            plugin("maven-publish")
            plugin("signing")
            plugin("org.jetbrains.dokka")
        }

        configure<KotlinJvmProjectExtension> {
            explicitApi()
        }

        afterEvaluate {
            configure<PublishingExtension> {
                publications {
                    register<MavenPublication>("jar") {
                        from(components.getByName("java"))
                        artifact(tasks["javadocJar"])

                        pom {
                            name.set(this@afterEvaluate.name)
                            description.set("Hooks is a little module for plugins, in Kotlin")
                            url.set("https://github.com/intuit/hooks")
                            licenses {
                                license {
                                    name.set("MIT")
                                    url.set("https://github.com/intuit/hooks/blob/master/LICENSE")
                                }
                            }
                            developers {
                                developer {
                                    id.set("sugarmanz")
                                    name.set("Jeremiah Zucker")
                                    email.set("zucker.jeremiah@gmail.com")
                                }
                            }
                            scm {
                                connection.set("scm:git:github.com/intuit/hooks.git")
                                developerConnection.set("scm:git:ssh://github.com/intuit/hooks.git")
                                url.set("https://github.com/intuit/hooks/tree/main")
                            }
                        }
                    }
                }
                repositories {
                    maven {
                        val version: String by project
                        name = "sonatype"
                        url = java.net.URI(
                            "https://oss.sonatype.org/" +
                                if (version.contains("-SNAPSHOT")) "content/repositories/snapshots/"
                                else "service/local/staging/deploy/maven2/"
                        )
                        credentials {
                            username = sonatypeUsername
                            password = sonatypePassword
                        }
                    }
                }
            }
        }

        configure<SigningExtension> {
            val signingKey by auth
            val signingPassword by auth
            useInMemoryPgpKeys(signingKey, signingPassword)
            sign(extensions.findByType(PublishingExtension::class.java)!!.publications)
        }

        tasks {
            register<Jar>("javadocJar") {
                dependsOn("dokkaJavadoc")
                archiveClassifier.set("javadoc")
                from("$buildDir/dokka/javadoc")
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
