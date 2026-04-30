package io.github.freya022.botcommands.api.emojis.exceptions

/**
 * Indicates that an application emoji has more than one definition with the same final emoji name.
 */
class EmojiAlreadyExistsException internal constructor(message: String) : RuntimeException(message)