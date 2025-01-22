package io.github.freya022.botcommands.api.core.filters.exceptions

/**
 * Indicates you attempted to use a filter not from the type required by the command.
 */
class InvalidFilterTypeException(message: String) : RuntimeException(message)