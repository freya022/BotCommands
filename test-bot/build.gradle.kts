plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
}

// The root project script is used to produce an aggregated POM

dependencies {
    testImplementation(libs.jda)
    testImplementation(projects.botCommandsCore)
    testImplementation(projects.botCommandsDatabase)
    testImplementation(projects.botCommandsCommands.text)
    testImplementation(projects.botCommandsCommands.app)
    testImplementation(projects.botCommandsAutocompleteCaffeine)
    testImplementation(projects.botCommandsComponents)
    testImplementation(projects.botCommandsModals)
    testImplementation(projects.botCommandsPagination)
    testImplementation(libs.kotlin.logging)
    testImplementation(projects.botCommandsJdaKtx)

    // Deserialization
    testImplementation(libs.jackson.databind)
    testImplementation(libs.jackson.module.kotlin)

    // Logging
    testImplementation(libs.logback.classic)

    // Database
    testRuntimeOnly(libs.postgresql)
    testRuntimeOnly(libs.h2)
    testImplementation(libs.flyway.core)
    testRuntimeOnly(libs.flyway.database.postgresql)
    testImplementation(libs.hikaricp)

    // Persistent rate limiting
    testImplementation(libs.bucket4j.jdk17.postgresql)

    // Upgrade because kotlinx-coroutines-debug somehow has an ANCIENT version
    testRuntimeOnly(libs.bytebuddy)
    testRuntimeOnly(libs.bytebuddy.agent)

    testRuntimeOnly(projects.botCommandsMethodAccessors.classfile)

    testImplementation(projects.botCommandsTypesafeMessages.core)
    testRuntimeOnly(projects.botCommandsTypesafeMessages.bc)
    testRuntimeOnly(projects.botCommandsTypesafeMessages.spring)

    implementation(projects.botCommandsRestarter)

    // ---------------------------- SPRING TEST BOT DEPENDENCIES ---------------------------

    // Spring module
    testImplementation(projects.botCommandsSpring)

    // Spring Boot
    testImplementation(libs.spring.boot.starter)
    testRuntimeOnly(libs.spring.boot.devtools)
}

tasks.withType<Test> {
    enabled = false
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
        )
    }
}
