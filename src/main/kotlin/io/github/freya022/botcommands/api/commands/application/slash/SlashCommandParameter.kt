package io.github.freya022.botcommands.api.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandParameter

interface SlashCommandParameter : ApplicationCommandParameter {
    override val nestedAggregatedParameters: List<SlashCommandParameter>
}