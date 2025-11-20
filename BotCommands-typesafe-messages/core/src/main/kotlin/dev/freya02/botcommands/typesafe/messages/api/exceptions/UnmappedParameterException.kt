package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent

/**
 * Exception when a function annotated with [@LocalizedContent][LocalizedContent]
 * has a parameter which's name, after transformation, doesn't exist as a template argument.
 */
@ExperimentalTypesafeMessagesApi
class UnmappedParameterException internal constructor(message: String) : IllegalArgumentException(message)
