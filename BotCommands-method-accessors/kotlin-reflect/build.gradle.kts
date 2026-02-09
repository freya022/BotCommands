import dev.freya02.botcommands.plugins.configureJarArtifact

plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
    id("publish-conventions")
}

dependencies {
    api(projects.botCommandsMethodAccessors.core)
}


publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-method-accessors-kotlin-reflect",
        description = "Provides support to call methods reflectively, using kotlin-reflect.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-method-accessors/kotlin-reflect",
    )
}
