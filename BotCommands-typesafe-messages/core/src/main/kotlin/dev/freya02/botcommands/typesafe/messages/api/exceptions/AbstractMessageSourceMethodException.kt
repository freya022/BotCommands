package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.IMessageSource
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent

/**
 * Exception when an interface extending [IMessageSource] has additional abstract functions without [@LocalizedContent][LocalizedContent].
 */
class AbstractMessageSourceMethodException internal constructor(message: String) : IllegalArgumentException(message)
