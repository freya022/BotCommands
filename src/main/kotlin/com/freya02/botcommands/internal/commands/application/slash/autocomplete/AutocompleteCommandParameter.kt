package com.freya02.botcommands.internal.commands.application.slash.autocomplete

import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.commands.application.slash.AbstractSlashCommandParameter
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.parameters.IAggregatedParameter
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.transform
import com.freya02.botcommands.internal.utils.ReflectionMetadata.isNullable
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignatureNoSrc
import kotlin.reflect.KFunction
import kotlin.reflect.full.findParameterByName

class AutocompleteCommandParameter(
    slashCommandInfo: SlashCommandInfo,
    slashCmdOptionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilder,
    autocompleteFunction: KFunction<*>
) : AbstractSlashCommandParameter(slashCommandInfo, slashCmdOptionAggregateBuilders, optionAggregateBuilder) {
    override val executableParameter = (autocompleteFunction.findParameterByName(name))?.also {
        requireUser(it.isNullable == kParameter.isNullable, autocompleteFunction) {
            "Parameter from autocomplete function '${kParameter.name}' should have same nullability as on slash command ${slashCommandInfo.method.shortSignatureNoSrc}"
        }
    } ?: throwUser(
        autocompleteFunction,
        "Parameter from autocomplete function '${kParameter.name}' should have been found on slash command ${slashCommandInfo.method.shortSignatureNoSrc}"
    )

    override val nestedAggregatedParameters: List<IAggregatedParameter> = optionAggregateBuilder.nestedAggregates.transform {
        AutocompleteCommandParameter(slashCommandInfo, it.nestedAggregates, it, optionAggregateBuilder.aggregator)
    }

    override fun constructOption(
        slashCommandInfo: SlashCommandInfo,
        optionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
        optionBuilder: SlashCommandOptionBuilder,
        resolver: SlashParameterResolver<*, *>
    ) = AutocompleteCommandOption(optionBuilder, resolver)
}