package com.freya02.botcommands.internal.commands.application.slash.autocomplete

import com.freya02.botcommands.api.commands.application.builder.OptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.CompositeKey
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.commands.application.slash.AbstractSlashCommandParameter
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation

class AutocompleteCommandParameter(
    slashCommandInfo: SlashCommandInfo,
    slashCmdOptionAggregateBuilders: Map<String, OptionAggregateBuilder>,
    parameter: KParameter,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilder
) : AbstractSlashCommandParameter(
    slashCommandInfo, slashCmdOptionAggregateBuilders, parameter, optionAggregateBuilder
) {
    @Suppress("UNCHECKED_CAST")
    override val commandOptions: List<AutocompleteCommandOption>
        get() = super.commandOptions as List<AutocompleteCommandOption>

    val isCompositeKey = parameter.hasAnnotation<CompositeKey>()

    override fun constructOption(
        slashCommandInfo: SlashCommandInfo,
        optionAggregateBuilders: Map<String, OptionAggregateBuilder>,
        optionBuilder: SlashCommandOptionBuilder,
        resolver: SlashParameterResolver<*, *>
    ) = AutocompleteCommandOption(slashCommandInfo, optionBuilder, resolver)
}