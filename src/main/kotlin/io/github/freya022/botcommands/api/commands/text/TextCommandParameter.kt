package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.commands.CommandParameter

/**
 * Represents a text command parameter,
 * which has its value computed by an aggregation function, from one or more options.
 */
interface TextCommandParameter : CommandParameter {
    override val nestedAggregatedParameters: List<TextCommandParameter>

    override val command: TextCommandVariation
}