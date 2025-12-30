plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
}

dependencies {
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
}

configurePublishedJarArtifact(
    artifactId = "BotCommands-method-accessors-core",
    description = "Provides an API to call methods reflectively.",
    url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-method-accessors/core",
)
