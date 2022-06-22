plugins {
    alias(libs.plugins.ksp)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)

    api(project(":hooks"))
    ksp(project(":processor"))

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.testing)
}

kotlin {
    explicitApi()

    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}
