import dev.freya02.botcommands.plugins.configureJarArtifact

plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
}

dependencies {
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)

    api(libs.jspecify)
}


publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-method-accessors-core",
        description = "Provides an API to call methods reflectively.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-method-accessors/core",
    )
}
