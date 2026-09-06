import nl.littlerobots.vcu.plugin.resolver.ModuleVersionCandidate
import nl.littlerobots.vcu.plugin.resolver.ModuleVersionSelector
import nl.littlerobots.vcu.plugin.resolver.VersionSelectors
import kotlin.io.path.Path
import kotlin.io.path.writeText

plugins {
    id("repositories-conventions")
    id("kotlin-conventions")
    id("publish-conventions")

    id("org.jetbrains.dokka")

    alias(libs.plugins.versionCatalogUpdate)
}

versionCatalogUpdate {
    versionSelector(object : ModuleVersionSelector {
        override fun select(candidate: ModuleVersionCandidate): Boolean {
            // Don't update major
            if (candidate.currentVersion[0] != candidate.candidate.version[0])
                return false

            return VersionSelectors.STABLE.select(candidate)
        }
    })
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

publishedProjectEnvironment {
    configureArtifact(
        artifactId = "BotCommands",
        packaging = "pom",
        description = "JDA framework with everything you need for a modern bot!",
        url = "https://github.com/freya022/BotCommands",
    )
}

tasks.register<DefaultTask>("getVersion") {
    group = "docs-publish"
    description = "Helper for the publish workflow to get the project's version"

    val version = publishedProjectEnvironment.version.get()
    doLast {
        val githubOutput = System.getenv("GITHUB_OUTPUT") ?: error("This task can only run in GitHub Actions")
        Path(githubOutput).writeText(
            """
                MAJOR_VERSION=${version.major}
                FULL_VERSION=$version
            """.trimIndent()
        )
    }
}
