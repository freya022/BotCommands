package io.github.freya022.botcommands.internal.commands.application.slash

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandResolverData
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.CommandOptions
import io.github.freya022.botcommands.internal.commands.application.ApplicationCommandParameterImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandOptionBuilderImpl

internal abstract class AbstractSlashCommandParameter internal constructor(
    final override val context: BContext,
    final override val command: SlashCommandInfoImpl,
    builder: SlashCommandBuilderImpl,
    slashCmdOptionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilderImpl
) : ApplicationCommandParameterImpl(context, optionAggregateBuilder, GlobalSlashEvent::class) {
    final override val options = CommandOptions.transform<SlashCommandOptionBuilderImpl, SlashParameterResolver<*, *>>(
        context,
        ApplicationCommandResolverData(builder),
        optionAggregateBuilder,
        optionFinalizer = { optionBuilder, resolver ->
            constructOption(context, command, builder, slashCmdOptionAggregateBuilders, optionBuilder, resolver)
        }
    )

    internal abstract fun constructOption(
        context: BContext,
        command: SlashCommandInfoImpl,
        builder: SlashCommandBuilderImpl,
        optionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
        optionBuilder: SlashCommandOptionBuilderImpl,
        resolver: SlashParameterResolver<*, *>
    ): AbstractSlashCommandOption
}