package io.github.freya022.botcommands.internal.commands.text.options

import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommandVariation
import io.github.freya022.botcommands.api.commands.text.options.TextCommandParameter
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.TextParameterResolver
import io.github.freya022.botcommands.internal.commands.options.CommandParameterImpl
import io.github.freya022.botcommands.internal.commands.text.options.builder.TextCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.text.options.builder.TextCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.options.CommandOptions
import io.github.freya022.botcommands.internal.options.transform

internal class TextCommandParameterImpl internal constructor(
    context: BContext,
    override val executable: TextCommandVariation,
    optionAggregateBuilder: TextCommandOptionAggregateBuilderImpl
) : CommandParameterImpl(context, optionAggregateBuilder, BaseCommandEvent::class),
    TextCommandParameter {

    override val nestedAggregatedParameters = optionAggregateBuilder.optionAggregateBuilders.transform {
        TextCommandParameterImpl(context, executable, it as TextCommandOptionAggregateBuilderImpl)
    }

    override val options = CommandOptions.transform<TextCommandOptionBuilderImpl, TextParameterResolver<*, *>>(
        context,
        executable,
        null,
        optionAggregateBuilder,
        optionFinalizer = { optionBuilder, resolver -> TextCommandOptionImpl(executable, optionBuilder, resolver) }
    )
}