import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(plugin(libs.plugins.kotlin))
    implementation(plugin(libs.plugins.mavenPublish))
    implementation(plugin(libs.plugins.dokka))

    implementation("dev.freya02:spring-configuration-metadata-generator")
}

tasks.withType<JavaCompile> {
    options.release = 17
}

// Kotlin configuration for the precompiled classes
kotlin {
    compilerOptions {
        // Version of the buildscript bytecode, so Gradle shuts up
        jvmTarget = JvmTarget.JVM_17

        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
        )
    }
}

// Helper function that transforms a Gradle Plugin alias from a
// Version Catalog into a valid dependency notation for buildSrc
@Suppress("UnusedReceiverParameter")
private fun DependencyHandlerScope.plugin(plugin: Provider<PluginDependency>) =
    plugin.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
