package com.freya02.botcommands.internal.core

// This really needs to not be critical
internal class Version private constructor(
    private val minor: Int,
    private val major: Int,
    private val revision: Int,
    private val classifierName: String,
    private val classifierVersion: Int
) : Comparable<Version> {
    override fun compareTo(other: Version): Int {
        if (minor != other.minor) return minor.compareTo(other.minor)
        if (major != other.major) return major.compareTo(other.major)
        if (revision != other.revision) return revision.compareTo(other.revision)
        if (classifierName != other.classifierName) return classifiers.indexOf(classifierName).compareTo(classifiers.indexOf(other.classifierName))
        if (classifierVersion != other.classifierVersion) return classifierVersion.compareTo(other.classifierVersion)

        return 0
    }

    companion object {
        private val classifiers = listOf("alpha", "beta", "release")

        fun getOrNull(versionString: String): Version? {
            val result = Regex("""(\d+).(\d+).(\d+)-(\w+)\.(\d+)(?:_(\w*))?""").matchEntire(versionString) ?: return null
            val (_, minor, major, revision, classifierName, classifierVersion) = result.groupValues

            if (classifierName !in classifiers) {
                return null
            }

            return Version(minor.toInt(), major.toInt(), revision.toInt(), classifierName, classifierVersion.toInt())
        }
    }
}

private operator fun <E> List<E>.component6() = get(5)
