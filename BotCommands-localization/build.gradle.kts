import dev.freya02.botcommands.plugins.configureJarArtifact
import dev.freya02.botcommands.utils.registerSourceSet

plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
}

// Register other source sets
// NOTE: Register them before dependencies, or you won't be able to add deps to them
// Use different source sets so we can use the same class names without clashes
registerSourceSet(name = "javaDocExamples")
registerSourceSet(name = "kotlinDocExamples")

val byteBuddyAgent: Configuration by configurations.creating

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

    // Deserialization
    implementation(libs.jackson.databind)
    implementation(libs.jackson.module.kotlin)

    // -------------------- SPRING DEPENDENCIES --------------------

    // Spring context
    compileOnly(libs.spring.context) // Optional

    // -------------------- DOC EXAMPLES DEPENDENCIES --------------------

    // YAML (de)serialization
    "javaDocExamplesImplementation"(libs.jackson.dataformat.yaml)
    "kotlinDocExamplesImplementation"(libs.jackson.dataformat.yaml)

    // -------------------- TEST DEPENDENCIES --------------------

    // JUnit + Mockk + Logback
    testImplementation(projects.testCommons)
    byteBuddyAgent(libs.bytebuddy.agent) { isTransitive = false }
}

tasks.withType<Test> {
    useJUnitPlatform()

    jvmArgs("-javaagent:${byteBuddyAgent.asPath}")
}

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-localization",
        description = "Common code for localization.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-rate-limit",
    )
}
