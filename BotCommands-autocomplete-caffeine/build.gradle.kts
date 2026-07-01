import dev.freya02.botcommands.plugins.configureJarArtifact

plugins {
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
}

val byteBuddyAgent = configurations.create("byteBuddyAgent")

dependencies {
    // JDA
    compileOnly(libs.jda)
    api(projects.botCommandsCore)
    api(projects.botCommandsCommands.app)

    // Caching
    implementation(libs.caffeine)

    // -------------------- TEST DEPENDENCIES --------------------

    // JUnit + Mockk + Logback
    testImplementation(projects.testCommons)
    byteBuddyAgent(libs.bytebuddy.agent) { isTransitive = false }
}

tasks.withType<Test> {
    useJUnitPlatform()

    jvmArgs("-javaagent:${byteBuddyAgent.asPath}")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
        )
    }
}

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-autocomplete-caffeine",
        description = "Provides a Caffeine-backed autocomplete cache.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-autocomplete-caffeine",
    )
}
