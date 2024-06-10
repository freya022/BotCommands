package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.transform

class SlashCommandParameter internal constructor(
    slashCommandInfo: SlashCommandInfoImpl,
    slashCmdOptionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilder
) : AbstractSlashCommandParameter(slashCommandInfo, slashCmdOptionAggregateBuilders, optionAggregateBuilder) {
    override val nestedAggregatedParameters = optionAggregateBuilder.nestedAggregates.transform {
        SlashCommandParameter(slashCommandInfo, slashCmdOptionAggregateBuilders, it)
    }

    override fun constructOption(
        slashCommandInfo: SlashCommandInfoImpl,
        optionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
        optionBuilder: SlashCommandOptionBuilder,
        resolver: SlashParameterResolver<*, *>
    ) = SlashCommandOption(slashCommandInfo, optionAggregateBuilders, optionBuilder, resolver)
}
