import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // Change in version catalog too
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
    implementation("com.vanniktech.maven.publish:com.vanniktech.maven.publish.gradle.plugin:0.32.0")
    implementation("org.jetbrains.dokka:org.jetbrains.dokka.gradle.plugin:2.0.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
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