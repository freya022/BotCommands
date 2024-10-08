package io.github.freya022.botcommands.api.commands.application.context.message.options

import io.github.freya022.botcommands.api.commands.application.context.options.ContextCommandOption

/**
 * Represents a Discord input option of a message context command.
 */
interface MessageContextCommandOption : ContextCommandOption {

    @Deprecated("Renamed to 'executable'", replaceWith = ReplaceWith("executable"))
    override val command get() = executable
    override val executable get() = parent.executable
    override val parent: MessageContextCommandParameter
}