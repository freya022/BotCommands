package io.github.freya022.botcommands.api.commands.application.context.message

import io.github.freya022.botcommands.api.commands.application.context.ContextCommandOption

/**
 * Represents a Discord input option of a message context command.
 */
interface MessageContextCommandOption : ContextCommandOption {
    override val command: MessageCommandInfo
}