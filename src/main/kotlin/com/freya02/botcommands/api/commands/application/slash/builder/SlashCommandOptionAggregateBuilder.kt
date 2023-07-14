package com.freya02.botcommands.api.commands.application.slash.builder

import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandOptionAggregateBuilder
import com.freya02.botcommands.api.commands.application.builder.ApplicationGeneratedOptionBuilder
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.builder.CustomOptionBuilder
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.parameters.AggregatorParameter
import com.freya02.botcommands.internal.utils.toDiscordString
import kotlin.reflect.KFunction

class SlashCommandOptionAggregateBuilder internal constructor(
    private val context: BContextImpl,
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : ApplicationCommandOptionAggregateBuilder<SlashCommandOptionAggregateBuilder>(aggregatorParameter, aggregator) {
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.toDiscordString(), block: SlashCommandOptionBuilder.() -> Unit = {}) {
        this += SlashCommandOptionBuilder(context, aggregatorParameter.toOptionParameter(aggregator, declaredName), optionName).apply(block)
    }

    override fun customOption(declaredName: String) {
        this += CustomOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    override fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        this += ApplicationGeneratedOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName), generatedValueSupplier)
    }

    @JvmOverloads
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
        SlashCommandOptionAggregateBuilder(context, aggregatorParameter, aggregator)
}