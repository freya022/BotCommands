package io.github.freya022.botcommands.internal.commands.application.slash.options

import io.github.freya022.botcommands.api.commands.application.slash.options.SlashCommandParameter
import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.options.builder.SlashCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.options.builder.SlashCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.options.transform

internal class SlashCommandParameterImpl internal constructor(
    context: BContext,
    command: SlashCommandInfoImpl,
    builder: SlashCommandBuilderImpl,
    slashCmdOptionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilderImpl
) : AbstractSlashCommandParameter(context, command, builder, slashCmdOptionAggregateBuilders, optionAggregateBuilder),
    SlashCommandParameter {

    override val nestedAggregatedParameters = optionAggregateBuilder.optionAggregateBuilders.transform {
        SlashCommandParameterImpl(context, command, builder, slashCmdOptionAggregateBuilders, it as SlashCommandOptionAggregateBuilderImpl)
    }

    override fun constructOption(
        context: BContext,
        command: SlashCommandInfoImpl,
        builder: SlashCommandBuilderImpl,
        optionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
        optionBuilder: SlashCommandOptionBuilderImpl,
        resolver: SlashParameterResolver<*, *>
    ) = SlashCommandOptionImpl(context, command, builder, optionAggregateBuilders, optionBuilder, resolver)
}
