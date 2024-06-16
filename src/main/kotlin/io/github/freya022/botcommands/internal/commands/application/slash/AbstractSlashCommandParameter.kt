package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandParameterImpl
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandResolverData

internal abstract class AbstractSlashCommandParameter internal constructor(
    final override val context: BContext,
    final override val command: SlashCommandInfoImpl,
    slashCmdOptionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilder
) : ApplicationCommandParameterImpl(context, optionAggregateBuilder) {
    final override val options = CommandOptions.transform<SlashCommandOptionBuilder, SlashParameterResolver<*, *>>(
        context,
        ApplicationCommandResolverData(command),
        optionAggregateBuilder,
        optionFinalizer = { optionBuilder, resolver ->
            constructOption(context, command, slashCmdOptionAggregateBuilders, optionBuilder, resolver)
        }
    )

    internal abstract fun constructOption(
        context: BContext,
        command: SlashCommandInfoImpl,
        optionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
        optionBuilder: SlashCommandOptionBuilder,
        resolver: SlashParameterResolver<*, *>
    ): AbstractSlashCommandOption
}