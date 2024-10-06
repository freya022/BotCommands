package io.github.freya022.botcommands.api.commands.application.context.user.options

import io.github.freya022.botcommands.api.commands.application.context.options.ContextCommandOption
import io.github.freya022.botcommands.api.commands.application.context.user.UserCommandInfo

/**
 * Represents a Discord input option of a user context command.
 */
interface UserContextCommandOption : ContextCommandOption {

    @Deprecated("Renamed to 'executable'", replaceWith = ReplaceWith("executable"))
    override val command get() = executable
    override val executable: UserCommandInfo
}