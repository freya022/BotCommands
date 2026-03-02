import nl.littlerobots.vcu.plugin.resolver.VersionSelectors

plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
    id("publish-conventions")

    id("org.jetbrains.dokka")

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

    testImplementation(libs.jda)

    // Architecture tests
    testImplementation(libs.konsist)
    testImplementation(libs.kotlin.metadata)

    // ---------------------- AGGREGATED DOCS DEPENDENCIES ----------------------

    dokka(projects.botCommandsCore)
    dokka(projects.botCommandsSpring)
    dokka(projects.botCommandsJdaKtx)
    dokka(projects.botCommandsMethodAccessors.core)
    dokka(projects.botCommandsTypesafeMessages.core)
    dokka(projects.botCommandsTypesafeMessages.bc)
    dokka(projects.botCommandsTypesafeMessages.spring)
    dokka(projects.botCommandsRestarter)
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

publishedProjectEnvironment {
    configureArtifact(
        artifactId = "BotCommands",
        packaging = "pom",
        description = "JDA framework with everything you need for a modern bot!",
        url = "https://github.com/freya022/BotCommands",
    )
}
