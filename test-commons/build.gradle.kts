plugins {
    id("kotlin-conventions")
}

dependencies {
    api(libs.jda)
    api(projects.botCommandsCore)
    compileOnly(projects.botCommandsDatabase)
    implementation(projects.botCommandsLocalization)

    implementation(libs.classgraph)

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
