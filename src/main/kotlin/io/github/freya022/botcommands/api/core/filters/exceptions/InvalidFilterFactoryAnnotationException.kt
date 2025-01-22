package io.github.freya022.botcommands.api.core.filters.exceptions

/**
 * Indicates you attempted to use a filter factory that doesn't support the same annotation.
 */
class InvalidFilterFactoryAnnotationException(message: String) : RuntimeException(message)