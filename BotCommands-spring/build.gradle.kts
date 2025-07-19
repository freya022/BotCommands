plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
    alias(libs.plugins.ksp)
}

dependencies {
    api(projects.botCommandsCore)
    implementation(projects.botCommandsJdaKtx)

    // Logging
    implementation(libs.kotlin.logging)

    // -------------------- SPRING DEPENDENCIES --------------------

    // Spring Boot
    api(libs.spring.boot)
    api(libs.spring.boot.autoconfigure)

    // -------------------- ANNOTATION PROCESSORS --------------------

    ksp(projects.springPropertiesProcessor)

    // -------------------- TEST DEPENDENCIES --------------------

    // Take the same test dependencies as the main library
    testImplementation(project.project(projects.botCommandsCore.path).sourceSets.test.get().compileClasspath)
    // Take the same test sources as the main library
    testImplementation(project.project(projects.botCommandsCore.path).sourceSets.test.get().output)

    // Spring Boot
    testImplementation(libs.spring.boot.starter)
    testRuntimeOnly(libs.spring.boot.devtools)
}

configurePublishedArtifact(artifactId = "BotCommands-spring")
