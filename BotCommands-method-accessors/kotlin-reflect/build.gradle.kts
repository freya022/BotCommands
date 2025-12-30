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

configurePublishedJarArtifact(
    artifactId = "BotCommands-method-accessors-kotlin-reflect",
    description = "Provides support to call methods reflectively, using kotlin-reflect.",
    url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-method-accessors/kotlin-reflect",
)
