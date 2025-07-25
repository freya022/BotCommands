package dev.freya02.botcommands.jda.ktx.deprecation.utils

inline fun <T : Collection<*>, R> T.ifNotEmpty(block: (T) -> R): R? {
    return if (isNotEmpty()) {
        block(this)
    } else {
        null
    }
}
