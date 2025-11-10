package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent

/**
 * Exception when a function annotated with [@LocalizedContent][LocalizedContent]
 * has a parameter which's name, after transformation, doesn't exist in as a template argument.
 */
class NoSuchTemplateArgumentException internal constructor(message: String) : IllegalArgumentException(message)
