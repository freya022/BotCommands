package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.CommandParameter

abstract class ApplicationCommandParameter(
    context: BContext,
    optionAggregateBuilder: ApplicationCommandOptionAggregateBuilder<*>
) : CommandParameter(context, optionAggregateBuilder)