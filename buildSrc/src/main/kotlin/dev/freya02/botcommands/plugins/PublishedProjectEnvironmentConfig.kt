package dev.freya02.botcommands.plugins

import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import dev.freya02.botcommands.utils.GitUtils
import dev.freya02.botcommands.utils.Version
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.assign
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.jetbrains.dokka.gradle.DokkaExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

abstract class PublishedProjectEnvironmentConfig(
    val project: Project
) {
    abstract val version: Property<Version>

    var isConfigured: Boolean = false
        private set

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

    val isJitpack = GitUtils.isJitpack(project.providers)

    val effectiveTag by lazy {
        if (canPublish) "v${version.get()}" else "3.X"
    }

    /**
     * Sets the provided [artifactId] as the Kotlin module name, Dokka module name & path, and Maven artifact ID.
     */
    fun configureArtifact(
        artifactId: String,
        packaging: String,
        description: String,
        url: String,
        block: MavenPublishBaseExtension.() -> Unit = {},
    ) {
        check(artifactId.startsWith("BotCommands")) {
            "Artifact ID must start with 'BotCommands'"
        }

        isConfigured = true

        project.tasks.withType<KotlinCompile> {
            compilerOptions {
                // Match up with Dokka's module path, as the wiki reconstructs wiki links with the module name
                moduleName = artifactId
            }
        }

        val dokkaExtension = project.extensions.findByType<DokkaExtension>()
        if (dokkaExtension != null) {
            dokkaExtension.apply {
                // For display in the nav sidebar
                moduleName = artifactId
                // For URLs consistent with the Kotlin module name
                modulePath = artifactId
            }
        }

        project.extensions.configure<MavenPublishBaseExtension>("mavenPublishing") {
            // Publish empty JAR
            // if the project has no API, and thus no docs,
            // or, we are publishing a snapshot (wasted disk space and CI time imo)
            // or, we can't publish anywhere (local build, dont waste time, docs generation should be tested using :dokkaGenerate)
            if (dokkaExtension == null || canPublishSnapshot || (!canPublishSnapshot && !canPublish)) {
                configure(KotlinJvm(javadocJar = JavadocJar.Empty()))
            }

            val groupId = if (isJitpack) {
                project.providers.environmentVariable("GROUP").get()
            } else {
                "io.github.freya022"
            }

            coordinates(groupId = groupId, artifactId = artifactId)

            pom {
                // Sonatype requires
                this.name = artifactId
                this.packaging = packaging

                this.description = description
                this.url = url
            }

            block()
        }
    }
}

/**
 * Sets the provided [artifactId] as the Kotlin module name, Dokka module name & path, and Maven artifact ID.
 */
fun PublishedProjectEnvironmentConfig.configureJarArtifact(
    artifactId: String,
    description: String,
    url: String,
    block: MavenPublishBaseExtension.() -> Unit = {},
) {
    configureArtifact(artifactId, "jar", description, url, block)
}
