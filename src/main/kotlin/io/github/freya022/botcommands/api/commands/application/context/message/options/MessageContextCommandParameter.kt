package io.github.freya022.botcommands.api.commands.application.context.message.options

import io.github.freya022.botcommands.api.commands.application.context.message.MessageCommandInfo
import io.github.freya022.botcommands.api.commands.application.context.options.ContextCommandParameter

/**
 * Represents a message context command parameter,
 * which has its value computed by an aggregation function, from one or more options.
 */
interface MessageContextCommandParameter : ContextCommandParameter {
    override val nestedAggregatedParameters: List<MessageContextCommandParameter>

    @Deprecated("Renamed to 'executable'", replaceWith = ReplaceWith("executable"))
    override val command: MessageCommandInfo get() = executable
    override val executable: MessageCommandInfo
}