package io.github.freya022.botcommands.api.core.exceptions

import io.github.freya022.botcommands.api.core.utils.retrieveThreadChannelById

/**
 * Exception thrown when retrieving a channel by ID, but the type is incorrect.
 *
 * @see retrieveThreadChannelById
 */
class InvalidChannelTypeException(message: String) : IllegalArgumentException(message)