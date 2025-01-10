package io.github.freya022.botcommands.api.emojis.exceptions

/**
 * Indicates that all application emoji slots were used.
 */
class OutOfAppEmojisException internal constructor(message: String) : RuntimeException(message)