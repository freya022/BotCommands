import dev.freya02.botcommands.plugins.configureJarArtifact

plugins {
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
}

dependencies {
    api(projects.botCommandsCore)

    // Logging
    implementation(libs.kotlin.logging)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=dev.freya02.botcommands.restarter.api.annotations.ExperimentalRestartApi",
        )
    }
}

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-restarter",
        description = "Enables restarting your bot on the same JVM during development.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-restarter",
    )
}
