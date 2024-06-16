package io.github.freya022.botcommands.api.commands.application.context.message

import io.github.freya022.botcommands.api.commands.application.context.ContextCommandParameter

/**
 * Represents a message context command parameter,
 * which has its value computed by an aggregation function, from one or more options.
 */
interface MessageContextCommandParameter : ContextCommandParameter {
    override val nestedAggregatedParameters: List<MessageContextCommandParameter>

    override val command: MessageCommandInfo
}