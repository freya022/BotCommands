package io.github.freya022.botcommands.api.emojis.exceptions

/**
 * Indicates that multiple resources were found for an application emoji's asset pattern.
 */
class NonUniqueEmojiResourceException internal constructor(message: String) : RuntimeException(message)