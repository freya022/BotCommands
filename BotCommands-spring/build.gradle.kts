plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
    alias(libs.plugins.ksp)
}

dependencies {
    api(projects.botCommandsCore)

    // Logging
    implementation(libs.kotlin.logging)

    // -------------------- SPRING DEPENDENCIES --------------------

    // Spring Boot
    api(libs.spring.boot)
    api(libs.spring.boot.autoconfigure)

    // -------------------- ANNOTATION PROCESSORS --------------------

    ksp(projects.springPropertiesProcessor)
}

configurePublishedArtifact(artifactId = "BotCommands-spring")
