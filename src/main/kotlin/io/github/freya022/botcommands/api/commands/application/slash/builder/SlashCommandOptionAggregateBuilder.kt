package io.github.freya022.botcommands.api.commands.application.slash.builder

import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import io.github.freya022.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.builder.CustomOptionBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.ParameterResolver
import io.github.freya022.botcommands.api.parameters.resolvers.SlashParameterResolver
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import io.github.freya022.botcommands.internal.utils.toDiscordString
import kotlin.reflect.KFunction

class SlashCommandOptionAggregateBuilder internal constructor(
    private val context: BContext,
    private val commandBuilder: SlashCommandBuilder,
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : ApplicationCommandOptionAggregateBuilder<SlashCommandOptionAggregateBuilder>(aggregatorParameter, aggregator) {
    /**
     * Declares an input option, supported types are in [ParameterResolver],
     * additional types can be added by implementing [SlashParameterResolver].
     *
     * @param declaredName Name of the declared parameter in the aggregator
     * @param optionName   Name of the option on Discord,
     * transforms the declared name uppercase characters with underscore + lowercase by default
     */
    fun option(declaredName: String, optionName: String = declaredName.toDiscordString(), block: SlashCommandOptionBuilder.() -> Unit = {}) {
        this += SlashCommandOptionBuilder(context, commandBuilder, aggregatorParameter.toOptionParameter(aggregator, declaredName), optionName).apply(block)
    }

    override fun customOption(declaredName: String) {
        this += CustomOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        this += ApplicationGeneratedOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName), generatedValueSupplier)
    }

    /**
     * Declares multiple input options in a single parameter.
     *
     * The parameter's type needs to be a [List],
     * where the element type is supported by [ParameterResolver].
     *
     * Additional types can be added by implementing [SlashParameterResolver].
     *
     * @param declaredName       Name of the declared parameter in the aggregator
     * @param amount             How many options to generate
     * @param requiredAmount     How many of the generated options are required
     * @param optionNameSupplier Block generating an option name from the option's index
     *
     * @see VarArgs
     */
    fun nestedOptionVararg(declaredName: String, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: SlashCommandOptionBuilder.(Int) -> Unit = {}) {
        //Same as in TextCommandVariationBuilder#optionVararg
        nestedVarargAggregate(declaredName) {
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