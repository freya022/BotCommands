import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")
    alias(libs.plugins.ksp)
}

// Register other source sets
// NOTE: Register them before dependencies, or you won't be able to add deps to them
registerSourceSet(name = "examples")
// Use different source sets so we can use the same class names without clashes
registerSourceSet(name = "javaDocExamples")
registerSourceSet(name = "kotlinDocExamples")

val byteBuddyAgent: Configuration by configurations.creating

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
    api(libs.jda) {
        exclude(module = "opus-java")
        exclude(module = "tink")
    }
    implementation(projects.botCommandsJdaKtx)

    // Classpath scanning
    api(libs.classgraph)

    api(projects.botCommandsMethodAccessors.core) // API due to opt-in annotation
    implementation(projects.botCommandsMethodAccessors.kotlinReflect)

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
    api(libs.jda.emojis) {
        exclude(module = "JDA")
    }

    // -------------------- AUTOCOMPLETE DEPENDENCIES --------------------

    // Fuzzy matching
    implementation(libs.java.string.similarity)

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

    // -------------------- DOC EXAMPLES DEPENDENCIES --------------------

    // YAML (de)serialization
    "javaDocExamplesImplementation"(libs.jackson.dataformat.yaml)
    "kotlinDocExamplesImplementation"(libs.jackson.dataformat.yaml)

    // Persistent rate limiting
    "javaDocExamplesImplementation"(libs.bucket4j.jdk17.postgresql)
    "kotlinDocExamplesImplementation"(libs.bucket4j.jdk17.postgresql)

    // -------------------- EXAMPLES DEPENDENCIES --------------------

    // Logging
    "examplesImplementation"(libs.logback.classic)

    // Coroutines
    "examplesImplementation"(libs.stacktrace.decoroutinator)

    // Database
    "examplesImplementation"(libs.h2)
    "examplesImplementation"(libs.flyway.core)

    // -------------------- TEST DEPENDENCIES --------------------

    testImplementation(libs.bundles.test)

    // Mocking
    testImplementation(libs.mockk)
    byteBuddyAgent(libs.bytebuddy.agent) { isTransitive = false }

    // Logging
    testImplementation(libs.logback.classic)

    // Database
    testImplementation(libs.h2)
    testImplementation(libs.flyway.core)
    testRuntimeOnly(libs.flyway.database.postgresql)

    testImplementation(projects.botCommandsMethodAccessors.classfile)

    // Test stuff
    testImplementation(libs.kotlin.metadata)
}

tasks.named<JavaCompile>("compileTestJava") {
    options.release = 24
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_24
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    jvmArgs("-javaagent:${byteBuddyAgent.asPath}")
}

val generateInfo by tasks.registering(GenerateBCInfoTask::class) {
    doNotTrackState("Can't know when Git hash/branch changes")
    outputs.upToDateWhen { false }
}

sourceSets {
    main {
        resources {
            srcDir(generateInfo)
        }
    }
}

dokka {
    dokkaSourceSets.configureEach {
        suppressGeneratedFiles = false
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
        )

        optIn.addAll(
            "io.github.freya022.botcommands.api.core.annotations.ExperimentalCoreApi"
        )
    }
}

configurePublishedJarArtifact(
    artifactId = "BotCommands-core",
    description = "Includes a core set of features bots typically need.",
    url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-core",
)
