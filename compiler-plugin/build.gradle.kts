dependencies {
    implementation(libs.ksp.spa)
    implementation(libs.ksp.poet)
    implementation(libs.ktlint.core)
    implementation(libs.ktlint.ruleset.standard)
    implementation(libs.arrow.core)

    testImplementation(project(":hooks"))
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.bundles.testing)
    testImplementation(libs.ksp.testing)
}
