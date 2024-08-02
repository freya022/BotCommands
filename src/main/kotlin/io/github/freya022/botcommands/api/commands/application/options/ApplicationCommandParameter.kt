package io.github.freya022.botcommands.api.commands.application.options

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.options.CommandParameter

/**
 * Represents an application command parameter,
 * which has its value computed by an aggregation function, from one or more options.
 */
interface ApplicationCommandParameter : CommandParameter {
    override val nestedAggregatedParameters: List<ApplicationCommandParameter>

    override val command: ApplicationCommandInfo
}