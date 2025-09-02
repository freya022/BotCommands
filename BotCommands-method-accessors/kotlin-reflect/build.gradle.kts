import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
}

dependencies {
    api(projects.botCommandsMethodAccessors.core)
}

tasks.withType<DokkaTask> {
    enabled = false
}

configurePublishedArtifact(artifactId = "BotCommands-method-accessors-kotlin-reflect")
