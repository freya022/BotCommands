package io.github.freya022.botcommands.api.core.messages.exceptions

/**
 * Exception thrown when a localization template could not be found
 * by a [BotCommandsMessages][io.github.freya022.botcommands.api.core.messages.BotCommandsMessages] instance.
 */
class MissingMessageTemplateException(message: String) : IllegalArgumentException(message)