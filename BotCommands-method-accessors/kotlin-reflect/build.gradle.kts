plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
}

dependencies {
    api(projects.botCommandsMethodAccessors.core)
}

configurePublishedArtifact(artifactId = "BotCommands-method-accessors-kotlin-reflect")
