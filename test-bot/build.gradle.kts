import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("BotCommands-conventions")
}

// The root project script is used to produce an aggregated POM

dependencies {
    implementation(projects.botCommandsCore)
    implementation(libs.kotlin.logging)
    implementation(projects.botCommandsJdaKtx)

    // Logging
    implementation(libs.logback.classic)

    // Coroutines
    implementation(libs.stacktrace.decoroutinator)

    // Database
    runtimeOnly(libs.postgresql)
    runtimeOnly(libs.h2)
    implementation(libs.flyway.core)
    runtimeOnly(libs.flyway.database.postgresql)
    implementation(libs.hikaricp)

    // Persistent rate limiting
    implementation(libs.bucket4j.jdk17.postgresql)

    // Upgrade because kotlinx-coroutines-debug somehow has an ANCIENT version
    runtimeOnly(libs.bytebuddy)
    runtimeOnly(libs.bytebuddy.agent)

    runtimeOnly(projects.botCommandsMethodAccessors.classfile)

    implementation(projects.botCommandsTypesafeMessages.core)
    runtimeOnly(projects.botCommandsTypesafeMessages.bc)

    // ---------------------------- SPRING TEST BOT DEPENDENCIES ---------------------------

    // Spring module
    implementation(projects.botCommandsSpring)

    // Spring Boot
    implementation(libs.spring.boot.starter)
    runtimeOnly(libs.spring.boot.devtools)
}

tasks.withType<Test> {
    enabled = false
}

tasks.withType<JavaCompile> {
    options.release = 24
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_24
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
        )
    }
}
