import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
    id("dokka-conventions")
}

dependencies {
    // -------------------- CORE DEPENDENCIES --------------------

    api(projects.botCommandsCore)

    // Logging
    implementation(libs.kotlin.logging)

    // Spring annotations
    compileOnly(libs.spring.context)

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
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

configurePublishedJarArtifact(
    artifactId = "BotCommands-typesafe-messages-core",
    description = "Easily define functions to retrieve (localized) messages, improving safety and convenience.",
    url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-typesafe-messages/core",
)
