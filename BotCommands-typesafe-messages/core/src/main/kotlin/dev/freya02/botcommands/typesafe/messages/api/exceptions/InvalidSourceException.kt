package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource

/**
 * Exception when a [IMessageSource] is invalid.
 */
class InvalidSourceException internal constructor(message: String) : IllegalArgumentException(message)
