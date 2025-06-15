import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.Directory
import org.gradle.api.logging.Logger
import org.gradle.api.provider.ProviderFactory
import java.io.IOException

object GitUtils {

    fun isCI(providers: ProviderFactory): Boolean {
        return (providers.systemProperty("BUILD_NUMBER").isPresent // Jenkins
                || providers.environmentVariable("BUILD_NUMBER").isPresent
                || providers.systemProperty("GIT_COMMIT").isPresent // Jitpack
                || providers.environmentVariable("GIT_COMMIT").isPresent
                || providers.systemProperty("GITHUB_ACTIONS").isPresent // GitHub Actions
                || providers.environmentVariable("GITHUB_ACTIONS").isPresent)
    }

    fun getCommitBranch(logger: Logger, providers: ProviderFactory, directory: String): String? {
        try {
            //Jitpack builds are detached from a branch, this will return the HEAD hash,
            // which can still be used on GitHub to get the state of the repository at that point
            val jitpackBranch = providers.environmentVariable("GIT_BRANCH").getOrNull()
            if (jitpackBranch != null) return jitpackBranch

            val output = providers.exec {
                commandLine("git", "rev-parse", "--abbrev-ref", "HEAD")
                workingDir(directory)
            }

            output.result.get().assertNormalExitValue()

            return output.standardOutput.asText.get().lineSequence().first()
        } catch (e: Exception) {
            logger.error("Unable to get commit branch", e)
            return null
        }
    }

    fun getCommitHash(logger: Logger, providers: ProviderFactory, directory: String): String? {
        try {
            val jitpackCommit = providers.environmentVariable("GIT_COMMIT").getOrNull()
            if (jitpackCommit != null) return jitpackCommit

            val output = providers.exec {
                commandLine("git", "rev-parse", "--verify", "HEAD")
                workingDir(directory)
            }

            output.result.get().assertNormalExitValue()

            return output.standardOutput.asText.get().lineSequence().first()
        } catch (e: Exception) {
            logger.error("Unable to get commit hash", e)
            return null
        }
    }

    fun getHeadTag(logger: Logger, providers: ProviderFactory, directory: String): String? {
        try {
            val output = providers.exec {
                commandLine("git", "describe", "--tags", "--abbrev=0", "--exact-match")
                workingDir(directory)
                isIgnoreExitValue = true
            }

            if (output.result.get().exitValue == 128) { return null }

            return output.standardOutput.asText.get().lineSequence().first()
        } catch (e: Exception) {
            logger.error("Unable to get head tag", e)
            return null
        }
    }
}