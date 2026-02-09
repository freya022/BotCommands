import dev.freya02.botcommands.plugins.configureJarArtifact

plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")

    alias(libs.plugins.ksp)
}

dependencies {
    // -------------------- CORE DEPENDENCIES --------------------

    api(libs.jda)
    api(libs.kotlinx.coroutines.core)

    ksp(projects.jdaKtxDeprecationProcessor)

    // -------------------- TEST DEPENDENCIES --------------------

    testImplementation(libs.bundles.test)

    testImplementation(libs.classgraph)
    testImplementation(libs.kotlin.reflect)
}

tasks.named<Test>("test") {
    useJUnitPlatform()

    environment("KSP_OUTPUT", layout.buildDirectory.dir("generated/ksp").get().asFile.path)
}

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-jda-ktx",
        description = "Kotlin extensions for JDA. This is recommended when using Kotlin for a more idiomatic usage.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-jda-ktx",
    )
}
