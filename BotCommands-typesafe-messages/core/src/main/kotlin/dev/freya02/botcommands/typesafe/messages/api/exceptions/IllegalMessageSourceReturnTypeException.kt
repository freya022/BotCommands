package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent

/**
 * Exception when a function annotated with [@LocalizedContent][LocalizedContent] has a wrong return type.
 */
class IllegalMessageSourceReturnTypeException internal constructor(message: String) : IllegalArgumentException(message)
