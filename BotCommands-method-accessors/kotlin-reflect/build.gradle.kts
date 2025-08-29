plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
}

dependencies {
    implementation(projects.botCommandsMethodAccessors.core)
}

configurePublishedArtifact(artifactId = "BotCommands-method-accessors-kotlin-reflect")
