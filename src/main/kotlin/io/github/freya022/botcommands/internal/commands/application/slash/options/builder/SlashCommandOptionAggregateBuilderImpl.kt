package io.github.freya022.botcommands.internal.commands.application.slash.options.builder

import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.slash.options.builder.SlashCommandOptionBuilder
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.options.builder.ApplicationCommandOptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.options.builder.ApplicationGeneratedOptionBuilderImpl
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandBuilderImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

internal class SlashCommandOptionAggregateBuilderImpl internal constructor(
    override val context: BContext,
    private val commandBuilder: SlashCommandBuilderImpl,
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : ApplicationCommandOptionAggregateBuilderImpl<SlashCommandOptionAggregateBuilder>(aggregatorParameter, aggregator),
    SlashCommandOptionAggregateBuilder {

    override val declarationSiteHolder: IDeclarationSiteHolder
        get() = commandBuilder

    override fun option(declaredName: String, optionName: String, block: SlashCommandOptionBuilder.() -> Unit) {
        this += SlashCommandOptionBuilderImpl(
            context,
            commandBuilder,
            aggregatorParameter.toOptionParameter(aggregator, declaredName),
            optionName
        ).apply(block)
    }

    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        this += ApplicationGeneratedOptionBuilderImpl(
            aggregatorParameter.toOptionParameter(aggregator, declaredName),
            generatedValueSupplier
        )
    }

    override fun optionVararg(declaredName: String, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: SlashCommandOptionBuilder.(Int) -> Unit) {
        //Same as in TextCommandVariationBuilder#optionVararg
        varargAggregate(declaredName) {
            for (i in 0..<amount) {
                option("args", optionNameSupplier(i)) {
                    block(i)
                    (this as SlashCommandOptionBuilderImpl).isOptional = i >= requiredAmount
                }
            }
        }
    }

    override fun constructNestedAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) =
        SlashCommandOptionAggregateBuilderImpl(context, commandBuilder, aggregatorParameter, aggregator)
}