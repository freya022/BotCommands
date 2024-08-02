package io.github.freya022.botcommands.api.commands.application.context.user.options

import io.github.freya022.botcommands.api.commands.application.context.options.ContextCommandOption
import io.github.freya022.botcommands.api.commands.application.context.user.UserCommandInfo

/**
 * Represents a Discord input option of a user context command.
 */
interface UserContextCommandOption : ContextCommandOption {
    override val command: UserCommandInfo
}