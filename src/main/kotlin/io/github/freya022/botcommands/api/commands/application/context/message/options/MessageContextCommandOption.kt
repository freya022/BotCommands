package io.github.freya022.botcommands.api.commands.application.context.message.options

import io.github.freya022.botcommands.api.commands.application.context.message.MessageCommandInfo
import io.github.freya022.botcommands.api.commands.application.context.options.ContextCommandOption

/**
 * Represents a Discord input option of a message context command.
 */
interface MessageContextCommandOption : ContextCommandOption {

    @Deprecated("Renamed to 'executable'", replaceWith = ReplaceWith("executable"))
    override val command get() = executable
    override val executable: MessageCommandInfo
}