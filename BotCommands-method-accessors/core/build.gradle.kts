plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
    id("dokka-conventions")
}

dependencies {
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)

    api(libs.jspecify)
}

configurePublishedJarArtifact(
    artifactId = "BotCommands-method-accessors-core",
    description = "Provides an API to call methods reflectively.",
    url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-method-accessors/core",
)
