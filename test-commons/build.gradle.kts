plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
}

dependencies {
    api(projects.botCommandsCore)

    api(libs.bundles.test)

    // Mocking
    api(libs.mockk)

    // Logging
    runtimeOnly(libs.logback.classic)
}
