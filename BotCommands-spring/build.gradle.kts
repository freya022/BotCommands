import dev.freya02.botcommands.plugins.configureJarArtifact
import dev.freya02.botcommands.utils.registerSpringFrameworkDocs

plugins {
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
    id("spring-configuration-metadata-conventions")
}

dependencies {
    compileOnly(libs.jda)
    api(projects.botCommandsCore)

    // Module configs
    compileOnly(projects.botCommandsAppEmojis)
    compileOnly(projects.botCommandsLocalization)
    compileOnly(projects.botCommandsComponents)
    compileOnly(projects.botCommandsDatabase)
    compileOnly(projects.botCommandsModals)
    compileOnly(projects.botCommandsCommands.text)
    compileOnly(projects.botCommandsCommands.app)

    // Logging
    implementation(libs.kotlinLogging)

    implementation(libs.classgraph)

    // -------------------- SPRING DEPENDENCIES --------------------

    // Spring Boot
    api(libs.springBoot)
    api(libs.springBoot.autoconfigure)

    compileOnly(libs.springBoot.devtools)
}

dokka {
    dokkaSourceSets.configureEach {
        registerSpringFrameworkDocs()
    }
}

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-spring",
        description = "Provides support for Spring Boot 3 and 4.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-spring",
    )
}
