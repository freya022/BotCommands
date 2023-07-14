package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.api.commands.prefixed.TextGeneratedValueSupplier
import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.internal.parameters.AggregatorParameter
import com.freya02.botcommands.internal.utils.toDiscordString
import kotlin.reflect.KFunction

class TextCommandOptionAggregateBuilder internal constructor(
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : OptionAggregateBuilder<TextCommandOptionAggregateBuilder>(aggregatorParameter, aggregator) {
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.toDiscordString(), block: TextCommandOptionBuilder.() -> Unit = {}) {
        this += TextCommandOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName), optionName).apply(block)
    }

    fun customOption(declaredName: String) {
        this += CustomOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    fun generatedOption(declaredName: String, generatedValueSupplier: TextGeneratedValueSupplier) {
        this += TextGeneratedOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName), generatedValueSupplier)
    }

    @JvmOverloads
    fun nestedOptionVararg(declaredName: String, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: TextCommandOptionBuilder.(Int) -> Unit = {}) {
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
        TextCommandOptionAggregateBuilder(aggregatorParameter, aggregator)
}
