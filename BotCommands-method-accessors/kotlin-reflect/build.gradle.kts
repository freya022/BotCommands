import org.jetbrains.dokka.gradle.tasks.DokkaBaseTask

plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
}

dependencies {
    api(projects.botCommandsMethodAccessors.core)
}

tasks.withType<DokkaBaseTask> {
    enabled = false
}

configurePublishedArtifact(artifactId = "BotCommands-method-accessors-kotlin-reflect")
