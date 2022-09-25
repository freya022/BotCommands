package com.freya02.botcommands.internal

fun Iterable<String>.joinWithQuote() = joinToString(separator = "', '", prefix = "'", postfix = "'")