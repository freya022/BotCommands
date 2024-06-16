package io.github.freya022.botcommands.api.commands.application.context.user

import io.github.freya022.botcommands.api.commands.application.context.ContextCommandOption

/**
 * Represents a Discord input option of a user context command.
 */
interface UserContextCommandOption : ContextCommandOption {
    override val command: UserCommandInfo
}