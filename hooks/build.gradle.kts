plugins {
    alias(libs.plugins.knit)
}

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.coroutines.core)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.testing)
}
