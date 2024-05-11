package io.github.freya022.botcommands.api.commands.text.builder

import io.github.freya022.botcommands.api.commands.builder.CustomOptionBuilder
import io.github.freya022.botcommands.api.commands.text.TextGeneratedValueSupplier
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.objectLogger
import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.api.core.utils.getSignature
import io.github.freya022.botcommands.internal.core.service.provider.canCreateWrappedService
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.resolveBestReference
import io.github.freya022.botcommands.internal.utils.toDiscordString
import kotlin.reflect.KFunction

class TextCommandOptionAggregateBuilder internal constructor(
    private val context: BContext,
    private val commandBuilder: TextCommandVariationBuilder,
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : OptionAggregateBuilder<TextCommandOptionAggregateBuilder>(aggregatorParameter, aggregator) {
    fun option(declaredName: String, optionName: String = declaredName.toDiscordString(), block: TextCommandOptionBuilder.() -> Unit = {}) {
        this += TextCommandOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName), optionName).apply(block)
    }

    fun customOption(declaredName: String) {
        if (context.serviceContainer.canCreateWrappedService(aggregatorParameter.typeCheckingParameter) == null) {
            objectLogger().warn { "Using ${this::customOption.resolveBestReference().getSignature(source = false)} **for services** has been deprecated, please use ${this::serviceOption.resolveBestReference().getSignature(source = false)} instead, parameter '$declaredName' of ${commandBuilder.declarationSite}" }
            return serviceOption(declaredName)
        }
        this += CustomOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    fun generatedOption(declaredName: String, generatedValueSupplier: TextGeneratedValueSupplier) {
        this += TextGeneratedOptionBuilder(aggregatorParameter.toOptionParameter(aggregator, declaredName), generatedValueSupplier)
    }

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
        TextCommandOptionAggregateBuilder(context, commandBuilder, aggregatorParameter, aggregator)
}
