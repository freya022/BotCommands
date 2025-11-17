package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory

/**
 * Exception when a [IMessageSourceFactory] is invalid.
 */
class InvalidSourceFactoryException internal constructor(message: String) : IllegalArgumentException(message)
