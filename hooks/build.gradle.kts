plugins {
    alias(libs.plugins.knit)
}

val COROUTINES_VERSION: String by project

dependencies {
    implementation(libs.kotlin.stdlib)
    implementation(libs.coroutines.core)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.testing)
}
