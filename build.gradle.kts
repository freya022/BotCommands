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

    // Architecture tests
    testImplementation(libs.konsist)

    // ---------------------- AGGREGATED DOCS DEPENDENCIES ----------------------

    dokka(projects.botCommandsCore)
    dokka(projects.botCommandsSpring)
}

configurePublishedArtifact(artifactId = "BotCommands", packaging = "pom")
