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

configurePublishedJarArtifact(
    artifactId = "BotCommands-spring",
    description = "Provides support for Spring Boot 3 and 4.",
    url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-spring",
)
