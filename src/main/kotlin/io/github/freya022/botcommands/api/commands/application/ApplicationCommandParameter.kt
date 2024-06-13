package io.github.freya022.botcommands.api.commands.application

import io.github.freya022.botcommands.api.commands.CommandParameter

interface ApplicationCommandParameter : CommandParameter {
    override val nestedAggregatedParameters: List<ApplicationCommandParameter>
}