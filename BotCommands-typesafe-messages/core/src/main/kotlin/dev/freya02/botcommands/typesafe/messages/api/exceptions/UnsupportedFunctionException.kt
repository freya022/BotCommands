package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent

/**
 * Exception when a function annotated with [@LocalizedContent][LocalizedContent] has unsupported characteristics.
 */
class UnsupportedFunctionException(message: String) : IllegalArgumentException(message)
