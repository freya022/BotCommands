package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory

/**
 * Exception when [IMessageSourceFactory] is not extended on an interface.
 */
class IllegalMessageSourceFactoryClassTypeException internal constructor(message: String) : IllegalArgumentException(message)
