import dev.freya02.botcommands.plugins.configureJarArtifact
import dev.freya02.botcommands.utils.registerSourceSet

plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
    id("spring-configuration-metadata-conventions")
}

// Use different source sets so we can use the same class names without clashes
registerSourceSet(name = "javaDocExamples")
registerSourceSet(name = "kotlinDocExamples")

val byteBuddyAgent: Configuration by configurations.creating

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
    api(projects.botCommandsRateLimit)
    api(projects.botCommandsLocalization)
    implementation(projects.botCommandsJdaKtx)

    // Database module
    implementation(projects.botCommandsDatabase)

    // Efficient data structures
    api(libs.trove4j.core)

    // Rate limiting
    implementation(libs.bucket4j.jdk17.core)

    // -------------------- SPRING DEPENDENCIES --------------------

    // Spring Boot (only for compatibility that cannot be put in a different module)
    compileOnly(libs.spring.boot.autoconfigure) // Optional

    // -------------------- DOC EXAMPLES DEPENDENCIES --------------------

    // Application commands
    "javaDocExamplesImplementation"(projects.botCommandsCommands.app)
    "kotlinDocExamplesImplementation"(projects.botCommandsCommands.app)

    // Components
    "javaDocExamplesImplementation"(projects.botCommandsComponents)
    "kotlinDocExamplesImplementation"(projects.botCommandsComponents)

    // -------------------- TEST DEPENDENCIES --------------------

    // JUnit + Mockk + Logback
    testImplementation(projects.testCommons)
    byteBuddyAgent(libs.bytebuddy.agent) { isTransitive = false }
}

tasks.withType<Test> {
    useJUnitPlatform()

    jvmArgs("-javaagent:${byteBuddyAgent.asPath}")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
        )
    }
}

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-components",
        description = "Support for components using annotated handlers and/or lambdas.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-components",
    )
}
