import nl.littlerobots.vcu.plugin.resolver.VersionSelectors
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")

    alias(libs.plugins.version.catalog.update)
}

versionCatalogUpdate {
    versionSelector(VersionSelectors.STABLE)
}

sourceSets {
    register("testBot")
}

// The root project script is used to produce an aggregated POM

dependencies {
    // -------------------- DEFAULT SUBPROJECTS DEPENDENCIES --------------------

    api(projects.botCommandsCore)

    // ---------------------------- TEST DEPENDENCIES ---------------------------

    // Architecture tests
    testImplementation(libs.konsist)
    testImplementation(libs.kotlin.metadata)

    // ---------------------------- TEST BOT DEPENDENCIES ---------------------------

    "testBotImplementation"(projects.botCommandsCore)
    "testBotImplementation"(libs.kotlin.logging)
    "testBotImplementation"(projects.botCommandsJdaKtx)

    // Logging
    "testBotImplementation"(libs.logback.classic)

    // Coroutines
    "testBotImplementation"(libs.stacktrace.decoroutinator)

    // Database
    "testBotRuntimeOnly"(libs.postgresql)
    "testBotRuntimeOnly"(libs.h2)
    "testBotImplementation"(libs.flyway.core)
    "testBotRuntimeOnly"(libs.flyway.database.postgresql)
    "testBotImplementation"(libs.hikaricp)

    // Persistent rate limiting
    "testBotImplementation"(libs.bucket4j.jdk17.postgresql)

    // Upgrade because kotlinx-coroutines-debug somehow has an ANCIENT version
    "testBotRuntimeOnly"(libs.bytebuddy)
    "testBotRuntimeOnly"(libs.bytebuddy.agent)

    "testBotRuntimeOnly"(projects.botCommandsMethodAccessors.classfile)

    // ---------------------------- SPRING TEST BOT DEPENDENCIES ---------------------------

    // Spring module
    "testBotImplementation"(projects.botCommandsSpring)

    // Spring Boot
    "testBotImplementation"(libs.spring.boot.starter)
    "testBotRuntimeOnly"(libs.spring.boot.devtools)

    // ---------------------- AGGREGATED DOCS DEPENDENCIES ----------------------

    dokka(projects.botCommandsCore)
    dokka(projects.botCommandsSpring)
    dokka(projects.botCommandsJdaKtx)
}

// This does not mean the library requires 24+, since the root module only has project-wide tests
java {
    sourceCompatibility = JavaVersion.VERSION_24
    targetCompatibility = JavaVersion.VERSION_24
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

configurePublishedArtifact(artifactId = "BotCommands", packaging = "pom")
