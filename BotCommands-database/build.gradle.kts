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
    compileOnly(libs.kotlinx.coroutines.debug) // Optional

    // Logging
    api(libs.slf4j.api)
    implementation(libs.kotlin.logging)

    // BC Core
    api(projects.botCommandsCore)

    // Efficient data structures
    implementation(libs.trove4j.core)

    // SQL connection pooling
    compileOnly(libs.hikaricp) // Optional

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
        artifactId = "BotCommands-database",
        description = "Small abstraction over JDBC with easier parameter binding, query logging, and leak detection.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-database",
    )
}
