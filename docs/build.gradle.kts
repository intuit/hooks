// 1. Apply Orchid plugin
plugins {
    alias(libs.plugins.orchid)
    alias(libs.plugins.knit)
    alias(libs.plugins.ksp)
}

// 2. Include Orchid dependencies
dependencies {
    orchidImplementation(libs.orchid.core)
    orchidImplementation(libs.orchid.copper)
    orchidRuntimeOnly(libs.bundles.orchid.plugins)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.testing)

    // generated test dependencies
    testImplementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlin.coroutines.core)
    testImplementation(libs.knit.testing)
    testImplementation(project(":hooks"))
    testImplementation(project(":processor"))
    ksp(project(":processor"))
}

// 4. Use the 'Editorial' theme, and set the URL it will have on Github Pages
orchid {
    githubToken = System.getenv("GH_TOKEN")
}

kotlin {
    sourceSets.test {
        kotlin.srcDir("build/generated/ksp/test/kotlin")
    }
}

tasks {
    test {
        dependsOn(knit)
    }
}
