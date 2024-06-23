package io.github.freya022.botcommands.api.commands.application.exceptions

/**
 * An exception thrown when pushing commands to Discord.
 */
class ApplicationCommandUpdateException internal constructor(
    message: String,
    cause: Throwable,
) : RuntimeException(message, cause)