package com.freya02.botcommands.api.core.utils

fun String.nullIfEmpty(): String? = when {
    isEmpty() -> null
    else -> this
}

fun Iterable<String>.joinWithQuote() = joinToString(separator = "', '", prefix = "'", postfix = "'")