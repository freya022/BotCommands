package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.TextCommandParameter
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.CommandParameterImpl
import io.github.freya022.botcommands.internal.transform

internal class TextCommandParameterImpl internal constructor(
    context: BContext,
    optionAggregateBuilder: TextCommandOptionAggregateBuilder
) : CommandParameterImpl(context, optionAggregateBuilder),
    TextCommandParameter {

    override val nestedAggregatedParameters = optionAggregateBuilder.nestedAggregates.transform {
        TextCommandParameterImpl(context, it)
    }

    override val options = CommandOptions.transform(
        context,
        null,
        optionAggregateBuilder,
        optionFinalizer = ::TextCommandOptionImpl
    )
}