import dev.freya02.botcommands.plugins.configureJarArtifact
import dev.freya02.botcommands.tasks.GenerateBCInfoTask
import dev.freya02.botcommands.utils.configureTests
import dev.freya02.botcommands.utils.registerBucket4JDocs
import dev.freya02.botcommands.utils.registerJetbrainsAnnotationsDocs
import dev.freya02.botcommands.utils.registerSourceSet

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

dependencies {
    // -------------------- CORE DEPENDENCIES --------------------

    // Kotlin
    api(libs.kotlin.reflect)
    api(libs.kotlinx.coroutines.core)

    // Logging
    api(libs.slf4j.api)
    implementation(libs.kotlin.logging)

    // JDA
    compileOnly(libs.jda)
    implementation(projects.botCommandsJdaKtx)

    // Classpath scanning
    implementation(libs.classgraph)

    api(projects.botCommandsMethodAccessors.core) // API due to opt-in annotation
    implementation(projects.botCommandsMethodAccessors.kotlinReflect)

    // -------------------- GLOBAL DEPENDENCIES --------------------

    // Efficient data structures
    implementation(libs.trove4j.core)

    // Rate limiting
    api(libs.bucket4j.jdk17.core)

    // -------------------- EMOJI DEPENDENCIES --------------------

    // JDA-specific emojis
    api(libs.jda.emojis)

    // -------------------- SPRING DEPENDENCIES --------------------

    // Spring Boot (only for compatibility that cannot be put in a different module)
    compileOnly(libs.spring.boot) // Optional
    compileOnly(libs.spring.boot.autoconfigure) // Optional

    // -------------------- ANNOTATION DEPENDENCIES --------------------

    api(libs.jsr305)
    compileOnly(libs.jetbrains.annotations)
    api(libs.jspecify)

    // -------------------- DOC EXAMPLES DEPENDENCIES --------------------

    // Text commands (rare examples that are about core features but use a different module to demonstrate)
    "javaDocExamplesImplementation"(projects.botCommandsCommands.text)
    "kotlinDocExamplesImplementation"(projects.botCommandsCommands.text)

    // -------------------- EXAMPLES DEPENDENCIES --------------------

    // Logging
    "examplesImplementation"(libs.logback.classic)

    // Database
    "examplesImplementation"(libs.hikaricp)
    "examplesImplementation"(projects.botCommandsDatabase)
    "examplesImplementation"(libs.h2)
    "examplesImplementation"(libs.flyway.core)

    // Deserialization
    "examplesImplementation"(libs.jackson.databind)
    "examplesImplementation"(libs.jackson.module.kotlin)

    // Text commands
    "examplesImplementation"(projects.botCommandsCommands.text)

    // Application commands
    "examplesImplementation"(projects.botCommandsCommands.app)
    "examplesImplementation"(projects.botCommandsAutocompleteCaffeine)

    // Components
    "examplesImplementation"(projects.botCommandsComponents)

    // Modals
    "examplesImplementation"(projects.botCommandsModals)

    // -------------------- TEST DEPENDENCIES --------------------

    // JUnit + Mockk + Logback
    testImplementation(projects.testCommons)

    testImplementation(projects.botCommandsMethodAccessors.classfile)
    testImplementation(projects.botCommandsLocalization)

    testImplementation(libs.kotlin.metadata)
}

configureTests(libs.bytebuddy.agent)

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
        registerBucket4JDocs()
        registerJetbrainsAnnotationsDocs()
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

publishedProjectEnvironment {
    configureJarArtifact(
        artifactId = "BotCommands-core",
        description = "Includes a core set of features bots typically need.",
        url = "https://github.com/freya022/BotCommands/tree/3.X/BotCommands-core",
    )
}
