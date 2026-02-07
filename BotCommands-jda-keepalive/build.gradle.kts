import dev.freya02.botcommands.plugins.configureJarArtifact
import dev.freya02.botcommands.utils.registerSourceSet
import dev.freya02.botcommands.utils.setMainJvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
}

// Register other source sets
registerSourceSet(name = "testBot")

dependencies {
    api(projects.botCommands)

    // Logging
    implementation(libs.kotlin.logging)

    // -------------------- TEST DEPENDENCIES --------------------

    testImplementation(libs.bundles.test)
    testImplementation(libs.mockk)
    testImplementation(libs.bytebuddy)
    testImplementation(libs.logback.classic)

    // ------------------ TEST BOT DEPENDENCIES ------------------

    "testBotImplementation"(projects.botCommandsRestarter)
    "testBotImplementation"(projects.botCommandsJdaKtx)
    "testBotImplementation"(libs.logback.classic)
}

setMainJvmTarget(target = 24)

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
            "-opt-in=dev.freya02.botcommands.jda.keepalive.api.ExperimentalKeepAliveApi",
        )
    }
}

tasks.named<KotlinCompile>("compileTestBotKotlin") {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=dev.freya02.botcommands.restarter.api.ExperimentalRestartApi",
        )
    }
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes(
            "Premain-Class" to "dev.freya02.botcommands.jda.keepalive.internal.Agent",
            "Agent-Class" to "dev.freya02.botcommands.jda.keepalive.internal.Agent",
        )
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    // Don't use "-javaagent" because [[AgentTest]] requires loading classes before the agent transforms them
    jvmArgs("-Djdk.attach.allowAttachSelf=true")
    jvmArgs("-Dbc.jda.keepalive.agentPath=${jar.archiveFile.get().asFile.absolutePath}")
}

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-jda-keepalive",
        description = "Provides caching support of your JDA instance between restarts.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-jda-keepalive",
    )
}
