import dev.freya02.botcommands.plugins.configureJarArtifact
import dev.freya02.botcommands.utils.configureTests

plugins {
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
}

dependencies {
    // JDA
    compileOnly(libs.jda)
    api(projects.botCommandsCore)
    api(projects.botCommandsCommands.app)

    // Caching
    implementation(libs.caffeine)

    // -------------------- TEST DEPENDENCIES --------------------

    // JUnit + Mockk + Logback
    testImplementation(projects.testCommons)
}

configureTests(libs.bytebuddy.agent)

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-autocomplete-caffeine",
        description = "Provides a Caffeine-backed autocomplete cache.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-autocomplete-caffeine",
    )
}
