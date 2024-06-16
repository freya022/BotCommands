package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete

import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.slash.AbstractSlashCommandParameter
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfoImpl
import io.github.freya022.botcommands.internal.transform
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.isNullable
import io.github.freya022.botcommands.internal.utils.requireUser
import io.github.freya022.botcommands.internal.utils.shortSignatureNoSrc
import io.github.freya022.botcommands.internal.utils.throwUser
import kotlin.reflect.KFunction
import kotlin.reflect.full.findParameterByName

internal class AutocompleteCommandParameterImpl internal constructor(
    context: BContext,
    command: SlashCommandInfoImpl,
    slashCmdOptionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilder,
    autocompleteFunction: KFunction<*>
) : AbstractSlashCommandParameter(context, command, slashCmdOptionAggregateBuilders, optionAggregateBuilder) {

    override val executableParameter = (autocompleteFunction.findParameterByName(name))?.also {
        requireUser(it.isNullable == kParameter.isNullable, autocompleteFunction) {
            "Parameter from autocomplete function '${kParameter.name}' should have same nullability as on slash command ${command.function.shortSignatureNoSrc}"
        }
    } ?: throwUser(
        autocompleteFunction,
        "Parameter from autocomplete function '${kParameter.name}' should have been found on slash command ${command.function.shortSignatureNoSrc}"
    )

    override val nestedAggregatedParameters = optionAggregateBuilder.nestedAggregates.transform {
        AutocompleteCommandParameterImpl(context, command, it.nestedAggregates, it, optionAggregateBuilder.aggregator)
    }

    override fun constructOption(
        context: BContext,
        command: SlashCommandInfoImpl,
        optionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
        optionBuilder: SlashCommandOptionBuilder,
        resolver: SlashParameterResolver<*, *>
    ) = AutocompleteCommandOptionImpl(context, command, optionBuilder, resolver)
}