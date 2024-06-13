package io.github.freya022.botcommands.internal.commands.application.context

import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.context.ContextCommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandParameterImpl

internal abstract class ContextCommandParameterImpl(
    context: BContext,
    optionAggregateBuilder: ApplicationCommandOptionAggregateBuilder<*>
) : ApplicationCommandParameterImpl(context, optionAggregateBuilder),
    ContextCommandParameter