package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.internal.commands.CommandParameter
import io.github.freya022.botcommands.internal.core.BContextImpl

abstract class ApplicationCommandParameter(
    context: BContextImpl,
    optionAggregateBuilder: ApplicationCommandOptionAggregateBuilder<*>
) : CommandParameter(context, optionAggregateBuilder)