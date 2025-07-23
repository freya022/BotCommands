import org.gradle.api.Project
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.dokka.gradle.DokkaExtension
import com.vanniktech.maven.publish.MavenPublishBaseExtension

private val configuredPublishedArtifacts = hashSetOf<String>()

fun isPublishedArtifactConfigured(project: Project) = project.path in configuredPublishedArtifacts

/**
 * Sets the provided [artifactId] as the Kotlin module name, Dokka module name & path, and Maven artifact ID.
 */
fun Project.configurePublishedArtifact(artifactId: String, packaging: String? = null) {
    check(artifactId.startsWith("BotCommands")) {
        "Artifact ID must start with 'BotCommands'"
    }

    configuredPublishedArtifacts += this.path

    tasks.withType<KotlinCompile> {
        compilerOptions {
            // Match up with Dokka's module path, as the wiki reconstructs wiki links with the module name
            moduleName = artifactId
        }
    }

    extensions.configure<DokkaExtension>("dokka") {
        // For display in the nav sidebar
        moduleName = artifactId
        // For URLs consistent with the Kotlin module name
        modulePath = artifactId
    }

    extensions.configure<MavenPublishBaseExtension>("mavenPublishing") {
        coordinates(artifactId = artifactId)

        pom {
            // Sonatype requires
            this.name = artifactId
            this.packaging = packaging
        }
    }
}
