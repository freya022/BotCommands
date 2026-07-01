package dev.freya02.botcommands.jda.ktx.retrieve

/**
 * Exception thrown when a guild was expected but not found, typically in sharded applications.
 */
class GuildNotFoundException internal constructor(message: String) : IllegalStateException(message)
