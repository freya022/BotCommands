package dev.freya02.botcommands.typesafe.messages.internal.utils

internal fun String.convertToCamelCase(): String {
    val builder = StringBuilder(this.length * 2)
    for (char in this) {
        if (char.isUpperCase()) {
            builder.append('_').append(char.lowercaseChar())
        } else {
            builder.append(char)
        }
    }
    return builder.toString()
}
