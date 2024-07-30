package io.github.freya022.botcommands.api.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.application.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.internal.commands.application.builder.ApplicationGeneratedOptionBuilder
import io.github.freya022.botcommands.internal.commands.application.slash.builder.SlashCommandBuilderImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

class SlashCommandOptionAggregateBuilder internal constructor(
    override val context: BContext,
    private val commandBuilder: SlashCommandBuilderImpl,
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : ApplicationCommandOptionAggregateBuilder<SlashCommandOptionAggregateBuilder>(aggregatorParameter, aggregator),
    SlashOptionRegistry {

    override val declarationSiteHolder: IDeclarationSiteHolder
        get() = commandBuilder

    override fun option(declaredName: String, optionName: String, block: SlashCommandOptionBuilder.() -> Unit) {
        this += SlashCommandOptionBuilder(context, commandBuilder, aggregatorParameter.toOptionParameter(aggregator, declaredName), optionName).apply(block)
    }

    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        this += ApplicationGeneratedOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName), generatedValueSupplier)
    }

    override fun optionVararg(declaredName: String, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: SlashCommandOptionBuilder.(Int) -> Unit) {
        //Same as in TextCommandVariationBuilder#optionVararg
        varargAggregate(declaredName) {
            for (i in 0..<amount) {
                option("args", optionNameSupplier(i)) {
                    block(i)
                    isOptional = i >= requiredAmount
                }
            }
        }
    }

    override fun constructNestedAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) =
        SlashCommandOptionAggregateBuilder(context, commandBuilder, aggregatorParameter, aggregator)
}