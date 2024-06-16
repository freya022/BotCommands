package io.github.freya022.botcommands.api.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandParameter

/**
 * Represents a slash command parameter,
 * which has its value computed by an aggregation function, from one or more options.
 */
interface SlashCommandParameter : ApplicationCommandParameter {
    override val nestedAggregatedParameters: List<SlashCommandParameter>

    override val command: SlashCommandInfo
}