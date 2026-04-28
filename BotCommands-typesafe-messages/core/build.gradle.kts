import dev.freya02.botcommands.plugins.configureJarArtifact
import dev.freya02.botcommands.utils.setMainJvmTarget

plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
}

dependencies {
    // -------------------- CORE DEPENDENCIES --------------------

    compileOnly(libs.jda)
    api(projects.botCommandsCore)
    compileOnly(projects.botCommandsCommands.text)

    // Logging
    implementation(libs.kotlin.logging)

    // Spring annotations
    compileOnly(libs.spring.boot.autoconfigure)

    // -------------------- TEST DEPENDENCIES --------------------

    testImplementation(libs.bundles.test)

    testImplementation(libs.jda)

    testImplementation(libs.mockk)
    testImplementation(libs.logback.classic)
}

setMainJvmTarget(target = 24)

kotlin {
    compilerOptions {
        optIn.add("dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi")
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-typesafe-messages-core",
        description = "Easily define functions to retrieve (localized) messages, improving safety and convenience.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-typesafe-messages/core",
    )
}
