package dev.freya02.botcommands.jda.ktx.deprecation.utils

fun String.suffixIfNotEmpty(suffix: String): String {
    return if (isNotEmpty())
        this + suffix
    else
        this
}
