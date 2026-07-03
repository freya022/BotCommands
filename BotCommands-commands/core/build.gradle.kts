import dev.freya02.botcommands.plugins.configureJarArtifact
import dev.freya02.botcommands.utils.configureTests

plugins {
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
}

dependencies {
    // -------------------- CORE DEPENDENCIES --------------------

    // Kotlin
    api(libs.kotlin.reflect)

    // Logging
    api(libs.slf4j.api)
    implementation(libs.kotlin.logging)

    // JDA
    compileOnly(libs.jda)
    api(projects.botCommandsCore)
    api(projects.botCommandsRateLimit)
    api(projects.botCommandsLocalization)

    implementation(libs.classgraph)

    // -------------------- SPRING DEPENDENCIES --------------------

    // Spring context
    compileOnly(libs.spring.context) // Optional

    // -------------------- TEST DEPENDENCIES --------------------

    // JUnit + Mockk + Logback
    testImplementation(projects.testCommons)
}

configureTests(libs.bytebuddy.agent)

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
        )
    }
}

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-commands-core",
        description = "Common code for text and application commands.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-commands/core",
    )
}
