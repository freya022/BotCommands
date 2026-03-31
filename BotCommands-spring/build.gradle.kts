import dev.freya02.botcommands.plugins.configureJarArtifact
import dev.freya02.botcommands.utils.registerSpringFrameworkDocs

plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
    id("spring-configuration-metadata-conventions")
}

dependencies {
    compileOnly(libs.jda)
    api(projects.botCommandsCore)

    // Module configs
    compileOnly(projects.botCommandsComponents)
    compileOnly(projects.botCommandsModals)

    // Logging
    implementation(libs.kotlin.logging)

    // -------------------- SPRING DEPENDENCIES --------------------

    // Spring Boot
    api(libs.spring.boot)
    api(libs.spring.boot.autoconfigure)

    compileOnly(libs.spring.boot.devtools)
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
