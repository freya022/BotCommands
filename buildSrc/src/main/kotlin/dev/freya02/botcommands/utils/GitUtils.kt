package dev.freya02.botcommands.utils

import org.gradle.api.logging.Logger
import org.gradle.api.provider.ProviderFactory

object GitUtils {

    fun isJitpack(providers: ProviderFactory): Boolean {
        return providers.systemProperty("GIT_COMMIT").isPresent
                || providers.environmentVariable("GIT_COMMIT").isPresent
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

            val githubCommit = providers.environmentVariable("PR_HEAD_SHA").getOrNull()
            if (githubCommit != null && githubCommit.isNotBlank()) return githubCommit

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
}
