package dev.freya02.botcommands.typesafe.messages.api.exceptions

import dev.freya02.botcommands.typesafe.messages.api.annotations.LocalizedContent
import net.dv8tion.jda.api.interactions.DiscordLocale
import java.util.*

/**
 * Exception when a function annotated with [@LocalizedContent][LocalizedContent] has a nullable parameter.
 *
 * **Note:** This does not apply to [DiscordLocale] or [Locale] parameters.
 */
class UnsupportedNullableParameterException(message: String) : IllegalArgumentException(message)
