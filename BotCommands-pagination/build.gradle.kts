import dev.freya02.botcommands.plugins.configureJarArtifact

plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
}

dependencies {
    // Kotlin coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Logging
    implementation(libs.slf4j.api)
    implementation(libs.kotlin.logging)

    // JDA
    compileOnly(libs.jda)
    api(projects.botCommandsCore)
    implementation(projects.botCommandsJdaKtx)
    api(projects.botCommandsComponents)
}

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-pagination",
        description = "Provides various types of paginations, using components.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-pagination",
    )
}
