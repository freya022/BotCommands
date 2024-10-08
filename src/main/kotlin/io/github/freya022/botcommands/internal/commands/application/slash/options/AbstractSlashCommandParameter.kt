package io.github.freya022.botcommands.internal.commands.application.slash.options

import io.github.freya022.botcommands.api.commands.application.ApplicationCommandResolverData
import io.github.freya022.botcommands.api.commands.application.slash.GlobalSlashEvent
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.options.ApplicationCommandParameterImpl
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.options.builder.SlashCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.options.builder.SlashCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.options.CommandOptions

internal abstract class AbstractSlashCommandParameter internal constructor(
    context: BContext,
    final override val executable: SlashCommandInfoImpl,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilderImpl
) : ApplicationCommandParameterImpl(context, optionAggregateBuilder, GlobalSlashEvent::class) {
    protected fun <P : AbstractSlashCommandParameter> transformOptions(
        parent: P,
        builder: SlashCommandBuilderImpl,
        optionAggregateBuilder: SlashCommandOptionAggregateBuilderImpl,
        block: (parent: P, optionBuilder: SlashCommandOptionBuilderImpl, resolver: SlashParameterResolver<*, *>) -> AbstractSlashCommandOption
    ): List<OptionImpl> {
        return CommandOptions.transform<SlashCommandOptionBuilderImpl, SlashParameterResolver<*, *>, P>(
            parent,
            ApplicationCommandResolverData(builder),
            optionAggregateBuilder,
            optionFinalizer = block
        )
    }
}