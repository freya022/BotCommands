plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
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

configurePublishedJarArtifact(
    artifactId = "BotCommands-jda-ktx",
    description = "Kotlin extensions for JDA. This is recommended when using Kotlin for a more idiomatic usage.",
    url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-jda-ktx",
)
