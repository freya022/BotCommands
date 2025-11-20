import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
}

dependencies {
    // -------------------- CORE DEPENDENCIES --------------------

    api(projects.botCommandsCore)
    api(projects.botCommandsSpring)
    api(projects.botCommandsTypesafeMessages.core)

    // Logging
    implementation(libs.kotlin.logging)

    implementation(libs.spring.boot)
    implementation(libs.spring.boot.autoconfigure)

    // -------------------- TEST DEPENDENCIES --------------------

    testImplementation(libs.bundles.test)

    testImplementation(libs.mockk)
    testImplementation(libs.logback.classic)

    testImplementation(projects.botCommandsSpring)
    testImplementation(libs.spring.boot.starter.test)
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

configurePublishedArtifact("BotCommands-typesafe-messages-spring")
