plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
}

dependencies {
    api(libs.jda)
    api(projects.botCommandsCore)

    api(libs.bundles.test)

    // Mocking
    api(libs.mockk)

    // Logging
    runtimeOnly(libs.logback.classic)

    // Database
    implementation(libs.h2)
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.database.postgresql)
}
