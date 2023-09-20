package com.freya02.botcommands.api.core.utils

fun String.nullIfEmpty(): String? = when {
    isEmpty() -> null
    else -> this
}

fun <T> Iterable<T>.joinWithQuote(transform: ((T) -> CharSequence)? = null) = joinToString(separator = "', '", prefix = "'", postfix = "'", transform = transform)

fun <T> Iterable<T>.joinAsList(transform: ((T) -> CharSequence)? = null) = joinToString(prefix = " - ", separator = "\n - ", transform = transform)