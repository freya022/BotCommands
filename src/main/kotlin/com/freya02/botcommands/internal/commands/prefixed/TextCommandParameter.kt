package com.freya02.botcommands.internal.commands.prefixed

import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.prefixed.builder.TextCommandOptionBuilder
import com.freya02.botcommands.api.parameters.RegexParameterResolver
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.CommandOptions
import com.freya02.botcommands.internal.commands.CommandParameter
import com.freya02.botcommands.internal.parameters.IAggregatedParameter
import com.freya02.botcommands.internal.transform

class TextCommandParameter(
    context: BContextImpl,
    optionAggregateBuilder: TextCommandOptionAggregateBuilder
) : CommandParameter(context, optionAggregateBuilder) {
    override val nestedAggregatedParameters: List<IAggregatedParameter> = optionAggregateBuilder.nestedAggregates.transform {
        TextCommandParameter(context, it)
    }

    override val commandOptions = CommandOptions.transform(
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