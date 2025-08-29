plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
}

dependencies {
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
}

configurePublishedArtifact(artifactId = "BotCommands-method-accessors-core")
