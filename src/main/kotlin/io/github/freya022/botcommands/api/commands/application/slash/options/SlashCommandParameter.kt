package io.github.freya022.botcommands.api.commands.application.slash.options

import io.github.freya022.botcommands.api.commands.application.options.ApplicationCommandParameter
import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandInfo

/**
 * Represents a slash command parameter,
 * which has its value computed by an aggregation function, from one or more options.
 */
interface SlashCommandParameter : ApplicationCommandParameter {
    override val nestedAggregatedParameters: List<SlashCommandParameter>

    @Deprecated("Renamed to 'executable'", replaceWith = ReplaceWith("executable"))
    override val command: SlashCommandInfo get() = executable
    override val executable: SlashCommandInfo
}