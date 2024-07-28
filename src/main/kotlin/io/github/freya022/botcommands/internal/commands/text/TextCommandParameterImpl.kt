package io.github.freya022.botcommands.internal.commands.text

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommandParameter
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.text.builder.TextCommandOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.CommandParameterImpl
import io.github.freya022.botcommands.internal.transform

internal class TextCommandParameterImpl internal constructor(
    override val context: BContext,
    override val command: TextCommandVariation,
    optionAggregateBuilder: TextCommandOptionAggregateBuilder
) : CommandParameterImpl(context, optionAggregateBuilder, BaseCommandEvent::class),
    TextCommandParameter {

    override val nestedAggregatedParameters = optionAggregateBuilder.optionAggregateBuilders.transform {
        TextCommandParameterImpl(context, command, it)
    }

    override val options = CommandOptions.transform<TextCommandOptionBuilder, TextParameterResolver<*, *>>(
        context,
        null,
        optionAggregateBuilder,
        optionFinalizer = { optionBuilder, resolver -> TextCommandOptionImpl(context, command, optionBuilder, resolver) }
    )
}