import dev.freya02.botcommands.plugins.configureJarArtifact
import dev.freya02.botcommands.utils.configureTests
import dev.freya02.botcommands.utils.registerSourceSet

plugins {
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
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

    // Logging
    api(libs.slf4j.api)
    implementation(libs.kotlin.logging)

    // JDA
    compileOnly(libs.jda)
    api(projects.botCommandsCore)
    implementation(projects.botCommandsJdaKtx)

    // -------------------- SPRING DEPENDENCIES --------------------

    // Spring context
    compileOnly(libs.spring.context) // Optional

    // -------------------- DOC EXAMPLES DEPENDENCIES --------------------

    // Application commands
    "javaDocExamplesImplementation"(projects.botCommandsCommands.app)
    "kotlinDocExamplesImplementation"(projects.botCommandsCommands.app)

    // Database
    "javaDocExamplesImplementation"(libs.hikaricp)
    "kotlinDocExamplesImplementation"(libs.hikaricp)
    "javaDocExamplesImplementation"(projects.botCommandsDatabase)
    "kotlinDocExamplesImplementation"(projects.botCommandsDatabase)

    // Persistent rate limiting
    "javaDocExamplesImplementation"(libs.bucket4j.jdk17.postgresql)
    "kotlinDocExamplesImplementation"(libs.bucket4j.jdk17.postgresql)

    // -------------------- TEST DEPENDENCIES --------------------

    // JUnit + Mockk + Logback
    testImplementation(projects.testCommons)
}

configureTests(libs.bytebuddy.agent)

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-rate-limit",
        description = "Common code for rate limiting.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-rate-limit",
    )
}
