package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi

/**
 * Exception when a [IMessageSource] is invalid.
 */
@ExperimentalTypesafeMessagesApi
class InvalidSourceException internal constructor(message: String) : IllegalArgumentException(message)
