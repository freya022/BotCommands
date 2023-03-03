package com.freya02.botcommands.internal

import com.freya02.botcommands.api.BCInfo
import com.freya02.botcommands.api.Logging
import net.dv8tion.jda.api.JDAInfo

// This really needs to not be critical
internal class Version private constructor(
    val minor: Int,
    val major: Int,
    val revision: Int,
    val classifier: Classifier?
) : Comparable<Version> {
    internal data class Classifier(val name: String, val version: Int) : Comparable<Classifier> {
        override fun compareTo(other: Classifier): Int {
            if (name != other.name) return classifierIndex().compareTo(other.classifierIndex())
            return version.compareTo(other.version)
        }

        private fun classifierIndex(): Int = classifiers.indexOf(name)
    }

    override fun compareTo(other: Version): Int {
        if (major != other.major) return major.compareTo(other.major)
        if (minor != other.minor) return minor.compareTo(other.minor)
        if (revision != other.revision) return revision.compareTo(other.revision)

        if (classifier != other.classifier) {
            return when {
                classifier == null -> 1 //This is a release
                other.classifier == null -> -1 //The other is a release
                else -> classifier.compareTo(other.classifier)
            }
        }

        return 0
    }

    companion object {
        private val logger = Logging.getLogger(Version::class.java)
        private val versionPattern = Regex("""(\d+)\.(\d+)\.(\d+)(?:-(\w+)\.(\d+))?(?:_\w*)?""")
        private val classifiers = listOf("alpha", "beta")

        @JvmStatic
        fun checkVersions() {
            logger.debug("Loading BotCommands ${BCInfo.VERSION} ; Compiled with JDA ${BCInfo.BUILD_JDA_VERSION} ; Running with JDA ${JDAInfo.VERSION}")

            val requiredJdaVersionStr = BCInfo.BUILD_JDA_VERSION
            val requiredJdaVersion = getOrNull(requiredJdaVersionStr) ?: let {
                logger.warn("Unrecognized built-with JDA version: $requiredJdaVersionStr")
                return
            }

            val currentJdaVersionStr = JDAInfo.VERSION
            val currentJdaVersion = getOrNull(currentJdaVersionStr) ?: let {
                logger.warn("Unrecognized JDA version: $currentJdaVersionStr")
                return
            }

            if (currentJdaVersion < requiredJdaVersion) {
                throw IllegalStateException("This bot is currently running JDA $currentJdaVersionStr but requires at least $requiredJdaVersionStr")
            }
        }

        fun get(versionString: String) =
            getOrNull(versionString) ?: throw IllegalArgumentException("Cannot parse version '$versionString'")

        fun getOrNull(versionString: String): Version? {
            val groups = versionPattern.matchEntire(versionString)?.groups ?: return null

            val major = groups[1]?.value?.toIntOrNull() ?: return null
            val minor = groups[2]?.value?.toIntOrNull() ?: return null
            val revision = groups[3]?.value?.toIntOrNull() ?: return null

            val classifierName = groups[4]?.value
            val classifier = classifierName?.let {
                if (it !in classifiers) return null

                val classifierVersion = groups[5]?.value?.toIntOrNull() ?: return null
                Classifier(it, classifierVersion)
            }

            return Version(minor, major, revision, classifier)
        }
    }
}
