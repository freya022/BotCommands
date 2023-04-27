package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.CommandOptions
import com.freya02.botcommands.internal.commands.application.ApplicationCommandParameter

abstract class AbstractSlashCommandParameter(
    slashCommandInfo: SlashCommandInfo,
    slashCmdOptionAggregateBuilders: Map<String, OptionAggregateBuilder>,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilder
) : ApplicationCommandParameter(slashCommandInfo.context, optionAggregateBuilder) {
    final override val commandOptions = CommandOptions.transform(
        slashCommandInfo.context,
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
        optionAggregateBuilders: Map<String, OptionAggregateBuilder>,
        optionBuilder: SlashCommandOptionBuilder,
        resolver: SlashParameterResolver<*, *>
    ): AbstractSlashCommandOption
}