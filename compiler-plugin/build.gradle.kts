dependencies {
    implementation(libs.ksp.spa)
//    implementation(libs.ksp.poet)
    implementation(libs.arrow.core)

    testImplementation(project(":hooks"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.testing)
    testImplementation(libs.ksp.testing)
}
