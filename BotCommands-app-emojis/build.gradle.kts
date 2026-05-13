import dev.freya02.botcommands.plugins.configureJarArtifact

plugins {
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

    // Logging
    api(libs.slf4j.api)
    implementation(libs.kotlin.logging)

    // JDA
    compileOnly(libs.jda)
    api(projects.botCommandsCore)

    implementation(libs.classgraph)

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

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-app-emojis",
        description = "Support for automatically managed application emojis using annotations.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-app-emojis",
    )
}
