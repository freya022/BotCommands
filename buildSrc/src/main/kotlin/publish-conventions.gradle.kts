import dev.freya02.botcommands.plugins.PublishedProjectEnvironmentConfig
import dev.freya02.botcommands.utils.GitUtils
import dev.freya02.botcommands.utils.Version

plugins {
    `java-library`
    signing
    id("com.vanniktech.maven.publish")
}

val environment = project.extensions.create<PublishedProjectEnvironmentConfig>("publishedProjectEnvironment")

environment.version = Version(
    major = providers.gradleProperty("version.major").get(),
    minor = providers.gradleProperty("version.minor").get(),
    revision = providers.gradleProperty("version.revision").get(),
    classifier = providers.gradleProperty("version.classifier").get().ifBlank { null },
)
version = environment.version.get()

java {
    withSourcesJar()
}

signing {
    isRequired = environment.canPublish || environment.canPublishSnapshot

    useInMemoryPgpKeys(environment.mavenGpgKeyId, environment.mavenGpgSecretKey, "")
}

publishing {
    if (environment.canPublishSnapshot) {
        repositories {
            maven("https://repo.freya02.dev/snapshots") {
                name = "Reposilite"

                credentials {
                    username = environment.reposiliteUsername
                    password = environment.reposilitePassword
                }
            }
        }
    }
}

mavenPublishing {
    if (environment.canPublish) {
        publishToMavenCentral(automaticRelease = true)
    }

    if (environment.canPublish || environment.canPublishSnapshot) {
        signAllPublications()
    }

    val pomVersion = if (environment.isJitpack) {
        providers.environmentVariable("VERSION").get()
    } else if (environment.canPublishSnapshot) {
        "${GitUtils.getCommitHash(logger, providers, projectDir.absolutePath)}-SNAPSHOT"
    } else if (environment.canPublish) {
        version.toString()
    } else {
        "${version}_DEV"
    }

    coordinates(version = pomVersion)

    pom {
        inceptionYear = "2020"

        licenses {
            license {
                name = "Mozilla Public License 2.0"
                url = "https://opensource.org/licenses/MPL-2.0"
                distribution = "repo"
            }
        }

        developers {
            developer {
                name = "freya022"
                email = "41875020+freya022@users.noreply.github.com"
                url = "https://github.com/freya022"
            }
        }

        scm {
            connection = "scm:git:https://github.com/freya022/BotCommands.git"
            developerConnection = connection
            url = "https://github.com/freya022/BotCommands"
            tag = environment.effectiveTag
        }
    }
}

afterEvaluate {
    check(environment.isConfigured) {
        "Project '${project.path}' did not call 'configurePublishedArtifact'"
    }
}
