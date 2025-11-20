package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory
import dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi

/**
 * Exception when a [IMessageSourceFactory] is invalid.
 */
@ExperimentalTypesafeMessagesApi
class InvalidSourceFactoryException internal constructor(message: String) : IllegalArgumentException(message)
