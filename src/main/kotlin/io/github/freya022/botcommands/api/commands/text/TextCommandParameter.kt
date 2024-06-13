package io.github.freya022.botcommands.api.commands.text

import io.github.freya022.botcommands.api.commands.CommandParameter

interface TextCommandParameter : CommandParameter {
    override val nestedAggregatedParameters: List<TextCommandParameter>
}