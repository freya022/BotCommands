package dev.freya02.botcommands.spring.metadata.generator.utils

internal fun String.tryAppendDot(): String = when {
    this.endsWith('.') -> this
    else -> "$this."
}
