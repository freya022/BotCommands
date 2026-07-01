package dev.freya02.botcommands.jda.ktx.retrieve

/**
 * Exception thrown when retrieving a channel by ID, but the type is incorrect.
 */
class InvalidChannelTypeException internal constructor(message: String) : IllegalArgumentException(message)
