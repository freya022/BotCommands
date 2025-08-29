import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
}

dependencies {
    implementation(projects.botCommandsMethodAccessors.core)
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

configurePublishedArtifact(artifactId = "BotCommands-method-accessors-classfile")
