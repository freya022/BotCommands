import dev.freya02.botcommands.plugins.configureJarArtifact

plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
}

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
    implementation(projects.botCommandsJdaKtx)

    // -------------------- SPRING DEPENDENCIES --------------------

    // Spring context
    compileOnly(libs.spring.context) // Optional

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
        artifactId = "BotCommands-rate-limit",
        description = "Common code for rate limiting.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-rate-limit",
    )
}
