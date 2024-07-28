package io.github.freya022.botcommands.internal.core.options.builder

import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.api.core.utils.unmodifiableView
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isVarargAggregator
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import kotlin.reflect.KFunction

internal class OptionAggregateBuilderContainerMixinImpl<T : OptionAggregateBuilder<T>> internal constructor(
    private val targetFunction: KFunction<*>,
    private val aggregateConstructor: (aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) -> T
) : OptionAggregateBuilderContainerMixin<T> {
    private val _optionAggregateBuilders: MutableMap<String, T> = mutableMapOf()

    override val optionAggregateBuilders: Map<String, T>
        get() = _optionAggregateBuilders.unmodifiableView()

    override fun hasVararg() = optionAggregateBuilders.values.any { it.aggregator.isVarargAggregator() }

    override fun aggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit) {
        aggregate(AggregatorParameter.fromUserAggregate(targetFunction, declaredName), aggregator, block)
    }

    override fun selfAggregate(declaredName: String, block: T.() -> Unit) {
        //When the option needs to be searched on the command function instead of the aggregator
        aggregate(AggregatorParameter.fromSelfAggregate(targetFunction, declaredName), InternalAggregators.theSingleAggregator, block)
    }

    override fun varargAggregate(declaredName: String, block: T.() -> Unit) {
        aggregate(AggregatorParameter.fromVarargAggregate(targetFunction, declaredName), InternalAggregators.theVarargAggregator, block)
    }

    private fun aggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>, block: T.() -> Unit) {
        _optionAggregateBuilders[aggregatorParameter.typeCheckingParameterName] = aggregateConstructor(aggregatorParameter, aggregator).apply(block)
    }
}