package dev.freya02.botcommands.jda.ktx.retrieve

import dev.freya02.botcommands.jda.ktx.DeprecatedInBcCore

/**
 * Exception thrown when retrieving a channel by ID, but the type is incorrect.
 *
 * @see retrieveThreadChannelById
 */
@DeprecatedInBcCore
class InvalidChannelTypeException(message: String) : IllegalArgumentException(message)
