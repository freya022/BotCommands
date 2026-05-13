import dev.freya02.botcommands.plugins.configureJarArtifact

plugins {
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
}

dependencies {
    // -------------------- CORE DEPENDENCIES --------------------

    compileOnly(libs.jda)
    api(libs.kotlinx.coroutines.core)

    // -------------------- TEST DEPENDENCIES --------------------

    testImplementation(libs.bundles.test)

    testImplementation(libs.jda)

    testImplementation(libs.classgraph)
    testImplementation(libs.kotlin.reflect)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-jda-ktx",
        description = "Kotlin extensions for JDA. This is recommended when using Kotlin for a more idiomatic usage.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-jda-ktx",
    )
}
