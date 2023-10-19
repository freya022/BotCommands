package io.github.freya022.botcommands.api.core.utils

fun String.nullIfEmpty(): String? = when {
    isEmpty() -> null
    else -> this
}

fun String.nullIfBlank(): String? = when {
    isBlank() -> null
    else -> this
}

fun <T> Iterable<T>.joinWithQuote(transform: ((T) -> CharSequence)? = null) = joinToString(separator = "', '", prefix = "'", postfix = "'", transform = transform)

fun <T> Iterable<T>.joinAsList(linePrefix: String = " -", transform: ((T) -> CharSequence)? = null) = joinToString(prefix = "$linePrefix ", separator = "\n$linePrefix ", transform = transform)