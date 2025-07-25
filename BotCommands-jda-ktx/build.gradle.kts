plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
    alias(libs.plugins.ksp)
}

dependencies {
    // -------------------- CORE DEPENDENCIES --------------------

    api(libs.jda)
    api(libs.kotlinx.coroutines.core)

    ksp(projects.jdaKtxDeprecationProcessor)

    // -------------------- TEST DEPENDENCIES --------------------

    testImplementation(libs.classgraph)
    testImplementation(libs.kotlin.reflect)
}

configurePublishedArtifact(artifactId = "BotCommands-jda-ktx")
