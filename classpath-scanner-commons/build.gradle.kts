import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlin)
}

group = "dev.freya02"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.classgraph)
}

tasks.named<JavaCompile>("compileJava") {
    options.release = 17
}

tasks.named<KotlinCompile>("compileKotlin") {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}
