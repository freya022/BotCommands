package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandParameterImpl
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandResolverData

internal abstract class AbstractSlashCommandParameter internal constructor(
    slashCommandInfo: SlashCommandInfoImpl,
    slashCmdOptionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilder
) : ApplicationCommandParameterImpl(slashCommandInfo.context, optionAggregateBuilder) {
    final override val options = CommandOptions.transform<SlashCommandOptionBuilder, SlashParameterResolver<*, *>>(
        slashCommandInfo.context,
        ApplicationCommandResolverData(slashCommandInfo),
        optionAggregateBuilder,
        optionFinalizer = { optionBuilder, resolver ->
            constructOption(slashCommandInfo, slashCmdOptionAggregateBuilders, optionBuilder, resolver)
        }
    )

    internal abstract fun constructOption(
        slashCommandInfo: SlashCommandInfoImpl,
        optionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
        optionBuilder: SlashCommandOptionBuilder,
        resolver: SlashParameterResolver<*, *>
    ): AbstractSlashCommandOption
}