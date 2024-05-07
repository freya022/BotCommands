package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandParameter
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandResolverData

abstract class AbstractSlashCommandParameter(
    slashCommandInfo: SlashCommandInfo,
    slashCmdOptionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilder
) : ApplicationCommandParameter(slashCommandInfo.context, optionAggregateBuilder) {
    final override val options = CommandOptions.transform(
        slashCommandInfo.context,
        ApplicationCommandResolverData(slashCommandInfo),
        optionAggregateBuilder,
        object : CommandOptions.Configuration<SlashCommandOptionBuilder, SlashParameterResolver<*, *>> {
            override fun transformOption(
                optionBuilder: SlashCommandOptionBuilder,
                resolver: SlashParameterResolver<*, *>
            ) = constructOption(slashCommandInfo, slashCmdOptionAggregateBuilders, optionBuilder, resolver)
        }
    )

    protected abstract fun constructOption(
        slashCommandInfo: SlashCommandInfo,
        optionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
        optionBuilder: SlashCommandOptionBuilder,
        resolver: SlashParameterResolver<*, *>
    ): AbstractSlashCommandOption
}