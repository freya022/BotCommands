plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
    alias(libs.plugins.ksp)
}

configurations.all {
    exclude(module = "opus-java")
    exclude(module = "tink")
}

dependencies {
    // -------------------- CORE DEPENDENCIES --------------------

    // Kotlin
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    compileOnly(libs.kotlinx.coroutines.debug) // Optional

    // Logging
    api(libs.slf4j.api)
    implementation(libs.kotlin.logging)

    // JDA
    api(libs.jda)
    api(libs.jda.ktx)

    // Classpath scanning
    api(libs.classgraph)

    // -------------------- GLOBAL DEPENDENCIES --------------------

    api(libs.kotlinx.datetime)

    // Deserialization
    api(libs.jackson.databind)
    api(libs.jackson.module.kotlin)

    // Efficient data structures
    api(libs.trove4j.core)

    // Rate limiting
    api(libs.bucket4j.jdk17.core)

    // -------------------- DATABASE DEPENDENCIES --------------------

    // SQL connection pooling
    compileOnly(libs.hikaricp) // Optional

    // -------------------- EMOJI DEPENDENCIES --------------------

    // All Unicode emojis
    api(libs.jemoji)
    // JDA-specific emojis
    api(libs.jda.emojis)

    // -------------------- AUTOCOMPLETE DEPENDENCIES --------------------

    // Fuzzy matching
    api(libs.java.string.similarity)

    // Caching
    implementation(libs.caffeine)

    // -------------------- SPRING DEPENDENCIES --------------------

    // Spring Boot (only for compatibility that cannot be put in a different module)
    compileOnly(libs.spring.boot) // Optional
    compileOnly(libs.spring.boot.autoconfigure) // Optional

    // -------------------- ANNOTATION DEPENDENCIES --------------------

    api(libs.jsr305)
    compileOnly(libs.jetbrains.annotations)

    // -------------------- ANNOTATION PROCESSORS --------------------

    ksp(projects.springPropertiesProcessor)

    // -------------------- TEST DEPENDENCIES --------------------

    // Mocking
    testImplementation(libs.mockk)

    // Logging
    testImplementation(libs.logback.classic)

    // Coroutines
    testImplementation(libs.stacktrace.decoroutinator)

    // Database
    testImplementation(libs.postgresql)
    testImplementation(libs.h2)
    testImplementation(libs.flyway.core)
    testImplementation(libs.flyway.database.postgresql)
    testImplementation(libs.hikaricp)

    // Persistent rate limiting
    testImplementation(libs.bucket4j.jdk17.postgresql)

    // YAML (de)serialization
    testImplementation(libs.jackson.dataformat.yaml)

    // Upgrade because kotlinx-coroutines-debug somehow has an ANCIENT version
    testImplementation(libs.byte.buddy)
    testImplementation(libs.byte.buddy.agent)

    // Test stuff
    testImplementation(libs.kotlin.metadata)

    // The Spring Boot module will include them at runtime,
    // but we need to make sure the main module works without it
    testCompileOnly(libs.spring.boot)
    testCompileOnly(libs.spring.boot.autoconfigure)

    dokka(rootProject)
    dokka(projects.botCommandsSpring)
}

val generateInfo by tasks.registering(GenerateBCInfoTask::class) {
    doNotTrackState("Can't know when Git hash/branch changes")
    outputs.upToDateWhen { false }
}

ksp {
    excludedSources.from(generateInfo)
}

sourceSets {
    main {
        java {
            srcDir(generateInfo)
            exclude("**/\$BCInfo.java")
        }
    }
}

// Register examples
sourceSets {
    register("examples") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

configurations["examplesApi"].extendsFrom(configurations["api"], configurations["testApi"])
configurations["examplesImplementation"].extendsFrom(configurations["implementation"], configurations["testImplementation"])
configurations["examplesCompileOnly"].extendsFrom(configurations["compileOnly"], configurations["testCompileOnly"])

dokka {
    dokkaSourceSets.configureEach {
        suppressedFiles.from("src/main/java/io/github/freya022/botcommands/api/\$BCInfo.java")
        suppressGeneratedFiles = false
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjvm-default=all",
            "-Xcontext-receivers",
            "-Xsuppress-warning=CONTEXT_RECEIVERS_DEPRECATED",
            "-Xconsistent-data-class-copy-visibility",
        )
    }
}
