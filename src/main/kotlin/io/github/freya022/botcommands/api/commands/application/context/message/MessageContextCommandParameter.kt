package io.github.freya022.botcommands.api.commands.application.context.message

import io.github.freya022.botcommands.api.commands.application.context.ContextCommandParameter

interface MessageContextCommandParameter : ContextCommandParameter {
    override val nestedAggregatedParameters: List<MessageContextCommandParameter>
}