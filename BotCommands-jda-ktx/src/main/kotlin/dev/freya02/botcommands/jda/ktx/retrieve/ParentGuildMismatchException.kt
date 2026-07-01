package dev.freya02.botcommands.jda.ktx.retrieve

/**
 * Exception thrown when retrieving a channel by ID from a guild, but their expected guild is incorrect.
 */
class ParentGuildMismatchException internal constructor(message: String) : IllegalArgumentException(message)
