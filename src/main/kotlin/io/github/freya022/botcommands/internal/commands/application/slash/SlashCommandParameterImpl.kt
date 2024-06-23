package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.slash.SlashCommandParameter
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.transform

internal class SlashCommandParameterImpl internal constructor(
    context: BContext,
    command: SlashCommandInfoImpl,
    builder: SlashCommandBuilder,
    slashCmdOptionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilder
) : AbstractSlashCommandParameter(context, command, builder, slashCmdOptionAggregateBuilders, optionAggregateBuilder),
    SlashCommandParameter {

    override val nestedAggregatedParameters = optionAggregateBuilder.nestedAggregates.transform {
        SlashCommandParameterImpl(context, command, builder, slashCmdOptionAggregateBuilders, it)
    }

    override fun constructOption(
        context: BContext,
        command: SlashCommandInfoImpl,
        builder: SlashCommandBuilder,
        optionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
        optionBuilder: SlashCommandOptionBuilder,
        resolver: SlashParameterResolver<*, *>
    ) = SlashCommandOptionImpl(context, command, builder, optionAggregateBuilders, optionBuilder, resolver)
}
