package io.github.freya022.botcommands.api.emojis.exceptions

/**
 * Indicates that no resource could be found for an application emoji's asset pattern.
 */
class NoEmojiResourceException internal constructor(message: String) : RuntimeException(message)