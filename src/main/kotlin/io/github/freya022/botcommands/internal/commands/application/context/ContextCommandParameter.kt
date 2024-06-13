package io.github.freya022.botcommands.internal.commands.application.context

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.CommandParameter

abstract class ContextCommandParameter(
    context: BContext,
    optionAggregateBuilder: ApplicationCommandOptionAggregateBuilder<*>
) : CommandParameter(context, optionAggregateBuilder)