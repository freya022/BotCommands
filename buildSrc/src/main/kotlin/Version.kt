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
}
