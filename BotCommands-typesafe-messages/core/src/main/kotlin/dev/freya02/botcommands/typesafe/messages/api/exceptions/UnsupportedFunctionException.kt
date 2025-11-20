package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent

/**
 * Exception when a function annotated with [@LocalizedContent][LocalizedContent] has unsupported characteristics.
 */
@ExperimentalTypesafeMessagesApi
class UnsupportedFunctionException(message: String) : IllegalArgumentException(message)
