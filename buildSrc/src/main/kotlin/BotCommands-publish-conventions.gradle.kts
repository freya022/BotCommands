plugins {
    `java-library`
    signing
    id("com.vanniktech.maven.publish")
    id("org.jetbrains.dokka")
}

val mavenCentralUsername: String? by project
val mavenCentralPassword: String? by project
/**
 * 1. Generate a key pair with `gpg --gen-key`, it will ask for a key name and an email address
 * 2. Start editing the key with `gpg --edit-key <key name>`, you should see a `gpg>` prompt
 * 3. (Optional) Modify the expiry of your primary key with `expire`
 * 4. If a subkey (`ssb`) was generated automatically, you can delete it by selecting it with `key 1` and running `delkey`
 * 5. Add a subkey with `addkey`, select `RSA (sign only)`
 * 6. Save everything with `save`, it will exit the gpg prompt
 * 7. Show the subkey IDs with `gpg -K --keyid-format short`, you should see two keys:
 *    - `sec ed25519/<primary key id> <created on> [SC]`, with the line below being the public key
 *    - `ssb rsa<length>/<subkey id> <created on> [S]`
 * 8. Set `mavenGpgKeyId` with the subkey id
 * 9. Set `mavenGpgSecretKey` with the secret key using `gpg --export-secret-key --armor <public key>`
 */
val mavenGpgKeyId: String? by project
val mavenGpgSecretKey: String? by project

val reposiliteUsername: String? by project
val reposilitePassword: String? by project

val canSign = mavenGpgKeyId != null && mavenGpgSecretKey != null
val canPublish = mavenCentralUsername != null && mavenCentralPassword != null && canSign
val canPublishSnapshot = reposiliteUsername?.isNotBlank() == true && reposilitePassword?.isNotBlank() == true && canSign

version = Version(
    major = providers.gradleProperty("version.major").get(),
    minor = providers.gradleProperty("version.minor").get(),
    revision = providers.gradleProperty("version.revision").get(),
    classifier = providers.gradleProperty("version.classifier").get(),
)

val effectiveTag = if (canPublish) {
    GitUtils.getHeadTag(logger, providers, projectDir.absolutePath) ?: error("Attempted to publish on a non-release commit")
} else {
    "3.X"
}

java {
    withSourcesJar()
}

dokka {
    dokkaSourceSets.configureEach {
        jdkVersion = 17

        perPackageOption {
            matchingRegex = ".*internal.*"
            suppress = true
        }

        sourceLink {
            localDirectory = rootDir
            remoteUrl("https://github.com/freya022/BotCommands/tree/${effectiveTag}")
            remoteLineSuffix = "#L"
        }

        externalDocumentationLinks.register("JDA") {
            url("https://docs.jda.wiki")
            packageListUrl("https://docs.jda.wiki/element-list")
        }

        externalDocumentationLinks.register("JetBrainsAnnotations") {
            url("https://javadoc.io/doc/org.jetbrains/annotations/26.0.2")
            packageListUrl("https://javadoc.io/doc/org.jetbrains/annotations/26.0.2/package-list")
        }

        externalDocumentationLinks.register("Spring") {
            url("https://docs.spring.io/spring-framework/docs/current/javadoc-api")
            packageListUrl("https://docs.spring.io/spring-framework/docs/current/javadoc-api/element-list")
        }

        externalDocumentationLinks.register("Bucket4J") {
            url("https://javadoc.io/doc/com.bucket4j/bucket4j_jdk17-core/8.14.0")
            packageListUrl("https://javadoc.io/doc/com.bucket4j/bucket4j_jdk17-core/8.14.0/element-list")
        }
    }
}

signing {
    isRequired = canPublish || canPublishSnapshot

    useInMemoryPgpKeys(mavenGpgKeyId, mavenGpgSecretKey, "")
}

publishing {
    if (canPublishSnapshot) {
        repositories {
            maven("https://repo.freya02.dev/snapshots") {
                name = "Reposilite"

                credentials {
                    username = reposiliteUsername
                    password = reposilitePassword
                }
            }
        }
    }
}

mavenPublishing {
    if (canPublish) {
        publishToMavenCentral(automaticRelease = true)
    }

    if (canPublish || canPublishSnapshot) {
        signAllPublications()
    }

    val pomVersion = if (GitUtils.isJitpack(providers)) {
        providers.environmentVariable("VERSION").get()
    } else if (canPublishSnapshot) {
        "${GitUtils.getCommitHash(logger, providers, projectDir.absolutePath)}-SNAPSHOT"
    } else if (canPublish) {
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
            tag = effectiveTag
        }
    }
}

afterEvaluate {
    check(isPublishedArtifactConfigured(project)) {
        "Project '${project.path}' did not call 'configurePublishedArtifact'"
    }
}
