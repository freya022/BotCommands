package com.freya02.botcommands.internal.commands.application.slash

import com.freya02.botcommands.api.commands.application.builder.OptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.SlashCommandOptionBuilder
import com.freya02.botcommands.api.parameters.ParameterWrapper.Companion.wrap
import com.freya02.botcommands.api.parameters.SlashParameterResolver
import com.freya02.botcommands.internal.CommandOptions
import com.freya02.botcommands.internal.commands.application.ApplicationCommandParameter
import com.freya02.botcommands.internal.parameters.ResolverContainer
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.simpleNestedName
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import kotlin.reflect.KParameter

abstract class AbstractSlashCommandParameter(
    slashCommandInfo: SlashCommandInfo,
    slashCmdOptionAggregateBuilders: Map<String, OptionAggregateBuilder>,
    parameter: KParameter,
    optionAggregateBuilder: SlashCommandOptionAggregateBuilder
) : ApplicationCommandParameter(parameter, optionAggregateBuilder) {
    override val commandOptions: List<AbstractSlashCommandOption> = optionAggregateBuilder.commandOptionBuilders.values
        .filterIsInstance<SlashCommandOptionBuilder>()
        .map { optionBuilder ->
            val resolver = slashCommandInfo.context
                .getService<ResolverContainer>()
                .getResolver(parameter.wrap())

            requireUser(resolver is SlashParameterResolver<*, *>, parameter.function) {
                "Expected a resolver of type ${SlashParameterResolver::class.simpleNestedName} but ${resolver.javaClass.simpleNestedName} does not support it"
            }

            constructOption(slashCommandInfo, slashCmdOptionAggregateBuilders, optionBuilder, resolver)
        }

    val options = CommandOptions.transform(
        slashCommandInfo.context,
        optionAggregateBuilder.commandOptionBuilders,
        Conf(slashCommandInfo, slashCmdOptionAggregateBuilders)
    )

    protected abstract fun constructOption(
        slashCommandInfo: SlashCommandInfo,
        optionAggregateBuilders: Map<String, OptionAggregateBuilder>,
        optionBuilder: SlashCommandOptionBuilder,
        resolver: SlashParameterResolver<*, *>
    ): AbstractSlashCommandOption

    private class Conf(
        private val slashCommandInfo: SlashCommandInfo,
        private val slashCmdOptionAggregateBuilders: Map<String, OptionAggregateBuilder>
    ) : CommandOptions.Configuration<SlashCommandOptionBuilder, SlashParameterResolver<*, *>> {
        override fun transformOption(optionBuilder: SlashCommandOptionBuilder, resolver: SlashParameterResolver<*, *>) =
            SlashCommandOption(slashCommandInfo, slashCmdOptionAggregateBuilders, optionBuilder, resolver)
    }
}