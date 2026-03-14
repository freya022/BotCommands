package io.github.freya022.botcommands.internal.commands.text.options.builder

import io.github.freya022.botcommands.api.commands.text.TextGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.text.options.builder.TextCommandOptionAggregateBuilder
import io.github.freya022.botcommands.api.commands.text.options.builder.TextCommandOptionBuilder
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlin.reflect.KFunction

internal class TextCommandOptionAggregateBuilderImpl internal constructor(
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : OptionAggregateBuilderImpl<TextCommandOptionAggregateBuilder>(aggregatorParameter, aggregator),
    TextCommandOptionAggregateBuilder {

    override fun option(declaredName: String, optionName: String, block: TextCommandOptionBuilder.() -> Unit) {
        this += TextCommandOptionBuilderImpl(
            aggregatorParameter.toOptionParameter(aggregator, declaredName),
            optionName
        ).apply(block)
    }

    override fun generatedOption(declaredName: String, generatedValueSupplier: TextGeneratedValueSupplier) {
        this += TextGeneratedOptionBuilderImpl(
            aggregatorParameter.toOptionParameter(aggregator, declaredName),
            generatedValueSupplier
        )
    }

    override fun optionVararg(declaredName: String, amount: Int, requiredAmount: Int, optionNameSupplier: (Int) -> String, block: TextCommandOptionBuilder.(Int) -> Unit) {
        if (hasVararg())
            throwArgument("Cannot have more than 1 vararg in text commands")
        require(amount > 0) { "Amount must be positive" }
        require(requiredAmount > 0) { "Required amount must be positive" }

        //Same as in TextCommandVariationBuilder#optionVararg
        varargAggregate(declaredName) {
            for (i in 0..<amount) {
                option("args", optionNameSupplier(i)) {
                    block(i)
                    (this as TextCommandOptionBuilderImpl).isOptional = i >= requiredAmount
                }
            }
        }
    }

    override fun constructNestedAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) =
        TextCommandOptionAggregateBuilderImpl(aggregatorParameter, aggregator)
}
