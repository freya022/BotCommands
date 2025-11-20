package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.annotations.ExperimentalTypesafeMessagesApi
import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent
import dev.freya02.botcommands.typesafe.messages.api.annotations.MessageSourceFactory

/**
 * Exception when a function annotated with [@LocalizedContent][LocalizedContent]
 * which's [template key][LocalizedContent.templateKey] doesn't exist in the root bundle defined by [@MessageSourceFactory][MessageSourceFactory].
 */
@ExperimentalTypesafeMessagesApi
class NoSuchTemplateKeyException(message: String) : IllegalArgumentException(message)
