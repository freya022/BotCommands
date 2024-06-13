package io.github.freya022.botcommands.internal.commands.application

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandParameter
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.CommandParameterImpl

internal abstract class ApplicationCommandParameterImpl internal constructor(
    context: BContext,
    optionAggregateBuilder: ApplicationCommandOptionAggregateBuilder<*>
) : CommandParameterImpl(context, optionAggregateBuilder),
    ApplicationCommandParameter {

    abstract override val nestedAggregatedParameters: List<ApplicationCommandParameterImpl>
}