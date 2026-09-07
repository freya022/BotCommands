plugins {
    id("kotlin-conventions")
}

dependencies {
    api(libs.jda)
    api(projects.botCommandsCore)

    api(libs.bundles.test)

    // Mocking
    api(libs.mockk)

    // Logging
    runtimeOnly(libs.logbackClassic)
}
