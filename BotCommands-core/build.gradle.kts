import dev.freya02.botcommands.plugins.configureJarArtifact
import dev.freya02.botcommands.tasks.GenerateBCInfoTask
import dev.freya02.botcommands.utils.registerBucket4JDocs
import dev.freya02.botcommands.utils.registerJetbrainsAnnotationsDocs
import dev.freya02.botcommands.utils.registerSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("kotlin-conventions")
    id("publish-conventions")
    id("dokka-conventions")
    id("spring-configuration-metadata-conventions")
}

// Register other source sets
// NOTE: Register them before dependencies, or you won't be able to add deps to them
registerSourceSet(name = "examples")
// Use different source sets so we can use the same class names without clashes
registerSourceSet(name = "javaDocExamples")
registerSourceSet(name = "kotlinDocExamples")

val byteBuddyAgent = configurations.create("byteBuddyAgent")

dependencies {
    // -------------------- CORE DEPENDENCIES --------------------

    // Kotlin
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)
    compileOnly(libs.kotlinx.coroutines.debug) // Optional

    // Logging
    api(libs.slf4j.api)
    implementation(libs.kotlinLogging)

    // JDA
    compileOnly(libs.jda)
    implementation(projects.botCommandsJdaKtx)

    // Classpath scanning
    api(libs.classgraph)

    api(projects.botCommandsMethodAccessors.core) // API due to opt-in annotation
    implementation(projects.botCommandsMethodAccessors.kotlinReflect)

    // -------------------- GLOBAL DEPENDENCIES --------------------

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
    api(libs.jdaEmojis) {
        exclude(module = "JDA")
    }

    // -------------------- AUTOCOMPLETE DEPENDENCIES --------------------

    // Fuzzy matching
    implementation(libs.javaStringSimilarity)

    // Caching
    implementation(libs.caffeine)

    // -------------------- SPRING DEPENDENCIES --------------------

    // Spring Boot (only for compatibility that cannot be put in a different module)
    compileOnly(libs.springBoot) // Optional
    compileOnly(libs.springBoot.autoconfigure) // Optional

    // -------------------- ANNOTATION DEPENDENCIES --------------------

    api(libs.jsr305)
    compileOnly(libs.jetbrainsAnnotations)
    api(libs.jspecify)

    // -------------------- DOC EXAMPLES DEPENDENCIES --------------------

    // YAML (de)serialization
    "javaDocExamplesImplementation"(libs.jackson.dataformat.yaml)
    "kotlinDocExamplesImplementation"(libs.jackson.dataformat.yaml)

    // Persistent rate limiting
    "javaDocExamplesImplementation"(libs.bucket4j.jdk17.postgresql)
    "kotlinDocExamplesImplementation"(libs.bucket4j.jdk17.postgresql)

    // -------------------- EXAMPLES DEPENDENCIES --------------------

    // Logging
    "examplesImplementation"(libs.logbackClassic)

    // Database
    "examplesImplementation"(libs.h2)
    "examplesImplementation"(libs.flyway.core)

    // -------------------- TEST DEPENDENCIES --------------------

    // JUnit + Mockk + Logback
    testImplementation(projects.testCommons)
    byteBuddyAgent(libs.bytebuddy.agent) { isTransitive = false }

    // Database
    testImplementation(libs.h2)
    testImplementation(libs.flyway.core)
    testRuntimeOnly(libs.flyway.database.postgresql)

    testImplementation(projects.botCommandsMethodAccessors.classfile)

    testImplementation(libs.kotlin.metadata)
}

tasks.withType<Test> {
    useJUnitPlatform()

    jvmArgs("-javaagent:${byteBuddyAgent.asPath}")
}

val generateInfo = tasks.register<GenerateBCInfoTask>("generateInfo") {
    description = "Generates the BCInfo data"

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

        registerBucket4JDocs()
        registerJetbrainsAnnotationsDocs()
    }
}

kotlin {
    compilerOptions {
        optIn.addAll(
            "io.github.freya022.botcommands.api.core.annotations.ExperimentalCoreApi"
        )
    }
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    compilerOptions {
        optIn.addAll(
            "io.github.freya022.botcommands.api.core.hooks.custom.annotations.ExperimentalCustomEvents"
        )
    }
}

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-core",
        description = "Includes a core set of features bots typically need.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-core",
    )
}
