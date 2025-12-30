import nl.littlerobots.vcu.plugin.resolver.VersionSelectors

plugins {
    id("BotCommands-conventions")
    id("BotCommands-publish-conventions")

    alias(libs.plugins.version.catalog.update)
}

versionCatalogUpdate {
    versionSelector(VersionSelectors.STABLE)
}

// The root project script is used to produce an aggregated POM

dependencies {
    // -------------------- DEFAULT SUBPROJECTS DEPENDENCIES --------------------

    api(projects.botCommandsCore)

    // ---------------------------- TEST DEPENDENCIES ---------------------------

    testImplementation(libs.bundles.test)

    // Architecture tests
    testImplementation(libs.konsist)
    testImplementation(libs.kotlin.metadata)

    // ---------------------- AGGREGATED DOCS DEPENDENCIES ----------------------

    dokka(projects.botCommandsCore)
    dokka(projects.botCommandsSpring)
    dokka(projects.botCommandsJdaKtx)
    dokka(projects.botCommandsTypesafeMessages.core)
    dokka(projects.botCommandsTypesafeMessages.bc)
    dokka(projects.botCommandsTypesafeMessages.spring)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xcontext-parameters",
        )
    }
}

configurePublishedArtifact(
    artifactId = "BotCommands",
    packaging = "pom",
    description = "JDA framework with everything you need for a modern bot!",
    url = "https://github.com/freya022/BotCommands",
)
