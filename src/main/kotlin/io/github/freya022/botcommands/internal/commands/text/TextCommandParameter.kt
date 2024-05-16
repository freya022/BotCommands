package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.builder.TextCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.CommandParameter
import io.github.freya022.botcommands.internal.parameters.IAggregatedParameter
import io.github.freya022.botcommands.internal.transform

class TextCommandParameter(
    context: BContext,
    optionAggregateBuilder: TextCommandOptionAggregateBuilder
) : CommandParameter(context, optionAggregateBuilder) {
    override val nestedAggregatedParameters: List<IAggregatedParameter> = optionAggregateBuilder.nestedAggregates.transform {
        TextCommandParameter(context, it)
    }

    override val options = CommandOptions.transform(
        context,
        null,
        optionAggregateBuilder,
        optionFinalizer = ::TextCommandOption
    )
}