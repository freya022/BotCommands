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

internal fun String.convertToSnakeCase(): String {
    val builder = StringBuilder(this.length)
    var nextIsUppercase = false
    for (char in this) {
        if (char == '_') {
            nextIsUppercase = true
        } else if (nextIsUppercase) {
            builder.append(char.uppercaseChar())
            nextIsUppercase = false
        } else {
            builder.append(char)
        }
    }
    return builder.toString()
}
