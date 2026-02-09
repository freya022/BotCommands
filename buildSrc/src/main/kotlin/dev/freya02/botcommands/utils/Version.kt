package dev.freya02.botcommands.utils

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

data class Version(
    val major: String,
    val minor: String,
    val revision: String,
    val classifier: String?,
) : Serializable {

    override fun toString(): String {
        return "$major.$minor.$revision-$classifier"
    }

    private fun writeObject(out: ObjectOutputStream) {
        out.defaultWriteObject()
    }

    private fun readObject(out: ObjectInputStream) {
        out.defaultReadObject()
    }
}
