package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.IMessageSourceFactory

/**
 * Exception when an interface extending [IMessageSourceFactory] has additional abstract methods.
 */
class AbstractMessageSourceFactoryMethodException internal constructor(message: String) : IllegalArgumentException(message)
