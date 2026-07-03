import dev.freya02.botcommands.plugins.configureJarArtifact
import dev.freya02.botcommands.utils.configureTests
import dev.freya02.botcommands.utils.registerSourceSet

plugins {
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
    id("spring-configuration-metadata-conventions")
}

// Register other source sets
// NOTE: Register them before dependencies, or you won't be able to add deps to them
// Use different source sets so we can use the same class names without clashes
registerSourceSet(name = "javaDocExamples")
registerSourceSet(name = "kotlinDocExamples")

dependencies {
    // -------------------- CORE DEPENDENCIES --------------------

    // Kotlin
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)

    // Logging
    api(libs.slf4j.api)
    implementation(libs.kotlin.logging)

    // JDA
    compileOnly(libs.jda)
    api(projects.botCommandsCore)
    api(projects.botCommandsCommands.core)
    api(projects.botCommandsRateLimit)
    api(projects.botCommandsLocalization)
    implementation(projects.botCommandsJdaKtx)

    // Database (optional, for commands caching)
    compileOnly(projects.botCommandsDatabase)

    // Deserialization
    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)

    // -------------------- GLOBAL DEPENDENCIES --------------------

    // Fuzzy matching
    implementation(libs.java.string.similarity)

    // Efficient data structures
    implementation(libs.trove4j.core)

    // -------------------- SPRING DEPENDENCIES --------------------

    // Spring Boot (only for compatibility that cannot be put in a different module)
    compileOnly(libs.spring.boot.autoconfigure) // Optional

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
        artifactId = "BotCommands-commands-app",
        description = "Support for application commands using annotated and declarative handlers, with smart registration.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-commands/app",
    )
}
