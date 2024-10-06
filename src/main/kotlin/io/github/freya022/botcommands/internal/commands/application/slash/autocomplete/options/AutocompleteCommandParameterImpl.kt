package io.github.freya022.botcommands.internal.commands.application.slash.autocomplete.options

import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.commands.application.slash.SlashCommandInfoImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.options.AbstractSlashCommandParameter
import io.github.freya022.botcommands.internal.commands.application.slash.options.builder.SlashCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.options.builder.SlashCommandOptionBuilderImpl
import io.github.freya022.botcommands.internal.options.transform
import io.github.freya022.botcommands.internal.utils.ReflectionMetadata.isNullable
import io.github.freya022.botcommands.internal.utils.requireAt
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlin.reflect.KFunction
import kotlin.reflect.full.findParameterByName

internal class AutocompleteCommandParameterImpl internal constructor(
    context: BContext,
    command: SlashCommandInfoImpl,
    builder: SlashCommandBuilderImpl,
    slashCmdOptionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilderImpl,
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

    override val nestedAggregatedParameters = optionAggregateBuilder.optionAggregateBuilders.transform {
        AutocompleteCommandParameterImpl(
            context,
            command,
            builder,
            (it as SlashCommandOptionAggregateBuilderImpl).optionAggregateBuilders,
            it,
            optionAggregateBuilder.aggregator
        )
    }

    override fun constructOption(
        command: SlashCommandInfoImpl,
        builder: SlashCommandBuilderImpl,
        optionAggregateBuilders: Map<String, SlashCommandOptionAggregateBuilder>,
        optionBuilder: SlashCommandOptionBuilderImpl,
        resolver: SlashParameterResolver<*, *>
    ) = AutocompleteCommandOptionImpl(command, optionBuilder, resolver)
}