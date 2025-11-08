import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("BotCommands-repositories-conventions")
    kotlin("jvm")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.isIncremental = true

    options.release = 17
    options.compilerArgs.add("-parameters")
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        jvmDefault = JvmDefaultMode.NO_COMPATIBILITY

        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xconsistent-data-class-copy-visibility",
        )
    }
}
