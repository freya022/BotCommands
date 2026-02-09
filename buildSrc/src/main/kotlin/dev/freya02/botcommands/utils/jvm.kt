package dev.freya02.botcommands.utils

import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

fun Project.setMainJvmTarget(target: Int) = setJvmTarget(target, taskPrefix = "compile")

fun Project.setJvmTarget(target: Int, taskPrefix: String) {
    tasks.withType<KotlinCompile> {
        if (name == "${taskPrefix}Kotlin") {
            compilerOptions {
                val target = JvmTarget.entries.find { it.name == "JVM_$target" }
                    ?: error("Invalid Kotlin JVM target: $target")
                jvmTarget.set(target)
            }
        }
    }

    tasks.withType<JavaCompile> {
        if (name == "${taskPrefix}Java") {
            options.release.set(target)
        }
    }
}
