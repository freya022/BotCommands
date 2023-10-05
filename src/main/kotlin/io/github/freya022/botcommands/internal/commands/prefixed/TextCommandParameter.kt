package io.github.freya022.botcommands.internal.commands.prefixed

import io.github.freya022.botcommands.api.commands.prefixed.builder.TextCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.prefixed.builder.TextCommandOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.RegexParameterResolver
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
        optionAggregateBuilder,
        object : CommandOptions.Configuration<TextCommandOptionBuilder, RegexParameterResolver<*, *>> {
            override fun transformOption(
                optionBuilder: TextCommandOptionBuilder,
                resolver: RegexParameterResolver<*, *>
            ) = TextCommandOption(optionBuilder, resolver)
        }
    )
}