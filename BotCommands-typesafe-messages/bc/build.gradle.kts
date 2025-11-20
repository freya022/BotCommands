import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
}

dependencies {
    // -------------------- CORE DEPENDENCIES --------------------

    api(projects.botCommandsCore)
    api(projects.botCommandsTypesafeMessages.core)

    // Logging
    implementation(libs.kotlin.logging)

    // -------------------- TEST DEPENDENCIES --------------------

    testImplementation(libs.bundles.test)

    testImplementation(libs.mockk)
    testImplementation(libs.logback.classic)
}

tasks.withType<JavaCompile> {
    options.release = 24
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_24
        optIn.add("dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configurePublishedArtifact("BotCommands-typesafe-messages-bc")
