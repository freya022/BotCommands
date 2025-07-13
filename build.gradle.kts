import nl.littlerobots.vcu.plugin.resolver.VersionSelectors

plugins {
    id("BotCommands-repositories-conventions")
    id("BotCommands-publish-conventions")

    alias(libs.plugins.version.catalog.update)
}

versionCatalogUpdate {
    versionSelector(VersionSelectors.STABLE)
}

// The root project script is used to produce an aggregated POM

dependencies {
    api(projects.botCommandsCore)
    api(projects.botCommandsSpring)

    // Aggregated docs
    dokka(projects.botCommandsCore)
    dokka(projects.botCommandsSpring)
}

mavenPublishing {
    pom {
        packaging = "pom"
    }
}
