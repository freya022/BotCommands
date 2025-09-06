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

tasks.withType<Test> {
    enabled = false // This module doesn't have unit tests yet
}

configurePublishedArtifact(artifactId = "BotCommands-spring")
