package io.github.freya022.botcommands.internal.localization

internal class RawArgument internal constructor(private val string: String) : LocalizableArgument {
    fun get() = string

    override fun toString(): String {
        return "RawArgument(string='$string')"
    }
}