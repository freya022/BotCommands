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

    // Architecture tests
    testImplementation(libs.konsist)

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
    testImplementation(libs.bytebuddy)
    testImplementation(libs.bytebuddy.agent)

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

fun registerSourceSet(name: String, extendsTestDependencies: Boolean) {
    sourceSets {
        register(name) {
            compileClasspath += sourceSets.main.get().output
            runtimeClasspath += sourceSets.main.get().output
        }
    }

    configurations["${name}Api"].extendsFrom(configurations["api"])
    configurations["${name}Implementation"].extendsFrom(configurations["implementation"])
    configurations["${name}CompileOnly"].extendsFrom(configurations["compileOnly"])

    if (extendsTestDependencies) {
        configurations["${name}Api"].extendsFrom(configurations["testApi"])
        configurations["${name}Implementation"].extendsFrom(configurations["testImplementation"])
        configurations["${name}CompileOnly"].extendsFrom(configurations["testCompileOnly"])
    }
}

// Register other source sets
registerSourceSet(name = "examples", extendsTestDependencies = true)
// Use different source sets so we can use the same class names without clashes
registerSourceSet(name = "javaDocExamples", extendsTestDependencies = true)
registerSourceSet(name = "kotlinDocExamples", extendsTestDependencies = true)

dokka {
    // Since this is the root module,
    // we need to override this property to make URLs predictable and more consistent
    modulePath = "BotCommands"

    dokkaSourceSets.configureEach {
        suppressedFiles.from("src/main/java/io/github/freya022/botcommands/api/\$BCInfo.java")
        suppressGeneratedFiles = false
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
        )
    }
}
