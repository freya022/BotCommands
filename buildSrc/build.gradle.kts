import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.kotlin.plugin)
    implementation(libs.maven.publish.plugin)
    implementation(libs.dokka.plugin)

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
