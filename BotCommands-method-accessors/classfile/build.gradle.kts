import org.jetbrains.dokka.gradle.tasks.DokkaBaseTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
}

dependencies {
    api(projects.botCommandsMethodAccessors.core)
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_24
    }
}

tasks.withType<DokkaBaseTask> {
    enabled = false
}

configurePublishedArtifact(artifactId = "BotCommands-method-accessors-classfile")
