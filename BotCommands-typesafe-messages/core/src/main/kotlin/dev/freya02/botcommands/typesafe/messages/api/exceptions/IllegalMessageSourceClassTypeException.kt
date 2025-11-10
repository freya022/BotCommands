package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource

/**
 * Exception when [IMessageSource] is not extended on an interface.
 */
class IllegalMessageSourceClassTypeException internal constructor(message: String) : IllegalArgumentException(message)
