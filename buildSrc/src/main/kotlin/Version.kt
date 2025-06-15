import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

data class Version(
    val major: String,
    val minor: String,
    val revision: String,
    val classifier: String?,
    val isDev: Boolean,
) : Serializable {

    override fun toString(): String {
        return "$major.$minor.$revision-$classifier" + let { if (isDev) "_DEV" else "" }
    }

    private fun writeObject(out: ObjectOutputStream) {
        out.defaultWriteObject()
    }

    private fun readObject(out: ObjectInputStream) {
        out.defaultReadObject()
    }

    companion object {
        private val versionPattern = Regex("""(\d+)\.(\d+)\.(\d+)(?:-(\w+\.\d+))?(?:_DEV)?""")

        fun parseOrNull(version: String): Version? {
            val groups = versionPattern.matchEntire(version)?.groups ?: return null

            val major = groups[1]?.value ?: return null
            val minor = groups[2]?.value ?: return null
            val revision = groups[3]?.value ?: return null
            val classifier = groups[4]?.value

            return Version(major, minor, revision, classifier, version.endsWith("_DEV"))
        }
    }
}