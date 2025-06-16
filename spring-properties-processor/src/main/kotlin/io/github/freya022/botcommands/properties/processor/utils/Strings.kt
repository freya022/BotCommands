package io.github.freya022.botcommands.properties.processor.utils

fun String.tryAppendDot(): String = when {
    this.endsWith('.') -> this
    else -> "$this."
}