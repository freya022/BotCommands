import org.jetbrains.dokka.gradle.tasks.DokkaBaseTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
}

dependencies {
    api(projects.botCommandsMethodAccessors.core)
}

tasks.withType<JavaCompile> {
    options.release = 24
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_24
    }
}

tasks.withType<DokkaBaseTask> {
    enabled = false
}

configurePublishedJarArtifact(
    artifactId = "BotCommands-method-accessors-classfile",
    description = "Provides support to call methods reflectively, using generated accessors.",
    url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-method-accessors/classfile",
)
