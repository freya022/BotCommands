import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java-conventions")
    kotlin("jvm")
}

kotlin {
    compilerOptions {
        jvmDefault = JvmDefaultMode.NO_COMPATIBILITY

        freeCompilerArgs.addAll(
            "-Xjsr305=strict",
            "-Xconsistent-data-class-copy-visibility",
            "-Xwarning-level=PLATFORM_CLASS_MAPPED_TO_KOTLIN:disabled",
            "-Xreturn-value-checker=check",
        )
    }
}

// Main source sets default to minimum Java version - 17
// Modules that use higher versions will override, while tests and other source sets uses the toolchain's version
tasks.named<KotlinCompile>("compileKotlin") {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}
