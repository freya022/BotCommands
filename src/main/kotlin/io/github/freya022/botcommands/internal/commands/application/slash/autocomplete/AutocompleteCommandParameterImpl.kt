package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.slash.AbstractSlashCommandParameter
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfoImpl
import io.github.freya022.botcommands.internal.transform
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.isNullable
import io.github.freya022.botcommands.internal.utils.requireAt
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlin.reflect.KFunction
import kotlin.reflect.full.findParameterByName

internal class AutocompleteCommandParameterImpl internal constructor(
    context: BContext,
    command: SlashCommandInfoImpl,
    builder: SlashCommandBuilder,
    slashCmdOptionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilder,
    autocompleteFunction: KFunction<*>
) : AbstractSlashCommandParameter(context, command, builder, slashCmdOptionAggregateBuilders, optionAggregateBuilder) {

    override val executableParameter =
        autocompleteFunction.findParameterByName(name)
            ?: throwArgument(
                autocompleteFunction,
                "Parameter from autocomplete function '${kParameter.name}' should have been found on slash command ${command.declarationSite}"
            )

    init {
        requireAt(executableParameter.isNullable == kParameter.isNullable, autocompleteFunction) {
            "Parameter from autocomplete function '${kParameter.name}' should have same nullability as on slash command ${command.declarationSite}"
        }
    }

    override val nestedAggregatedParameters = optionAggregateBuilder.nestedAggregates.transform {
        AutocompleteCommandParameterImpl(context, command, builder, it.nestedAggregates, it, optionAggregateBuilder.aggregator)
    }

    override fun constructOption(
        context: BContext,
        command: SlashCommandInfoImpl,
        builder: SlashCommandBuilder,
        optionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
        optionBuilder: SlashCommandOptionBuilder,
        resolver: SlashParameterResolver<*, *>
    ) = AutocompleteCommandOptionImpl(context, command, optionBuilder, resolver)
}