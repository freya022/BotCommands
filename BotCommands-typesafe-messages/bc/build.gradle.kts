import dev.freya02.botcommands.plugins.configureJarArtifact
import dev.freya02.botcommands.utils.setMainJvmTarget

plugins {
    id("kotlin-conventions")
    id("publish-conventions")
}

val byteBuddyAgent: Configuration by configurations.creating

dependencies {
    // -------------------- CORE DEPENDENCIES --------------------

    api(projects.botCommandsCore)
    api(projects.botCommandsTypesafeMessages.core)

    // Logging
    implementation(libs.kotlin.logging)

    implementation(libs.classgraph)

    // -------------------- TEST DEPENDENCIES --------------------

    // JUnit + Mockk + Logback
    testImplementation(projects.testCommons)
    byteBuddyAgent(libs.bytebuddy.agent) { isTransitive = false }

    testImplementation(projects.botCommandsLocalization)
}

setMainJvmTarget(target = 24)

kotlin {
    compilerOptions {
        optIn.add("dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    jvmArgs("-javaagent:${byteBuddyAgent.asPath}")
}

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-typesafe-messages-bc",
        description = "Easily define functions to retrieve (localized) messages, improving safety and convenience. Default version.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-typesafe-messages/bc",
    )
}
