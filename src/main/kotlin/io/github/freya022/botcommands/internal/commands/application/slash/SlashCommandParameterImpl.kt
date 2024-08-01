package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandParameter
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.transform

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
