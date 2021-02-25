plugins {
    id("kotlinx-knit")
}

val COROUTINES_VERSION: String by project

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-core", COROUTINES_VERSION)

    testImplementation(platform("org.junit:junit-bom:5.7.0"))
    testImplementation("org.junit.jupiter", "junit-jupiter")
    testImplementation("io.mockk", "mockk", "1.10.2")
}
