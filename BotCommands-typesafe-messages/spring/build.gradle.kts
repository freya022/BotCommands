import dev.freya02.botcommands.plugins.configureJarArtifact
import dev.freya02.botcommands.utils.setMainJvmTarget

plugins {
    id("kotlin-conventions")
    id("publish-conventions")
}

dependencies {
    // -------------------- CORE DEPENDENCIES --------------------

    api(projects.botCommandsCore)
    api(projects.botCommandsSpring)
    api(projects.botCommandsTypesafeMessages.core)

    // Logging
    implementation(libs.kotlinLogging)

    implementation(libs.springBoot)
    implementation(libs.springBoot.autoconfigure)

    // -------------------- TEST DEPENDENCIES --------------------

    testImplementation(libs.bundles.test)

    testImplementation(libs.jda)

    testImplementation(libs.mockk)
    testImplementation(libs.logbackClassic)

    testImplementation(projects.botCommandsSpring)
    testImplementation(libs.springBoot.starter.test)
}

setMainJvmTarget(target = 24)

kotlin {
    compilerOptions {
        optIn.add("dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-typesafe-messages-spring",
        description = "Easily define functions to retrieve (localized) messages, improving safety and convenience. Spring version.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-typesafe-messages/spring",
    )
}
