import dev.freya02.botcommands.plugins.configureJarArtifact

plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
    id("spring-configuration-metadata-conventions")
}

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
    api(projects.botCommandsCommands.core)
    implementation(projects.botCommandsJdaKtx)

    // Database (optional, for commands caching)
    compileOnly(projects.botCommandsDatabase)

    // -------------------- GLOBAL DEPENDENCIES --------------------

    // Fuzzy matching
    implementation(libs.java.string.similarity)

    // -------------------- SPRING DEPENDENCIES --------------------

    // Spring Boot (only for compatibility that cannot be put in a different module)
    compileOnly(libs.spring.boot.autoconfigure) // Optional

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
        artifactId = "BotCommands-commands-app",
        description = "Support for application commands using annotated and declarative handlers, with smart registration.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-commands/app",
    )
}
