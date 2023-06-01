package com.freya02.botcommands.internal.core.options.builder

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.internal.core.options.builder.InternalAggregators.isVarargAggregator
import com.freya02.botcommands.internal.core.options.builder.InternalAggregators.theSingleAggregator
import com.freya02.botcommands.internal.core.options.builder.InternalAggregators.theVarargAggregator
import com.freya02.botcommands.internal.parameters.AggregatorParameter
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

internal class OptionAggregateBuildersImpl<T : OptionAggregateBuilder<T>>(
    private val commandFunction: KFunction<*>,
    val aggregateConstructor: (aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) -> T
) {
    val optionAggregateBuilders: MutableMap<String, T> = mutableMapOf()

    fun aggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit) {
        aggregate(AggregatorParameter.fromUserAggregate(commandFunction, declaredName), aggregator, block)
    }

    fun selfAggregate(declaredName: String, block: T.() -> Unit) {
        //When the option needs to be searched on the command function instead of the aggregator
        aggregate(AggregatorParameter.fromSelfAggregate(commandFunction, declaredName), theSingleAggregator, block)
    }

    fun varargAggregate(declaredName: String, block: T.() -> Unit) {
        aggregate(AggregatorParameter.fromVarargAggregate(commandFunction, declaredName), theVarargAggregator, block)
    }

    private fun aggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>, block: T.() -> Unit) {
        optionAggregateBuilders[aggregatorParameter.typeCheckingParameterName] = aggregateConstructor(aggregatorParameter, aggregator).apply(block)
    }

    fun hasVararg() = optionAggregateBuilders.values.any { it.aggregator.isVarargAggregator() }
}

@BService
internal object InternalAggregators {
    internal val theSingleAggregator = InternalAggregators::singleAggregator.reflectReference()
    internal val theVarargAggregator = InternalAggregators::varargAggregator.reflectReference()

    internal fun KFunction<*>.isSingleAggregator() = this === theSingleAggregator
    internal fun KFunction<*>.isVarargAggregator() = this === theVarargAggregator
    internal fun KFunction<*>.isSpecialAggregator() = isSingleAggregator() || isVarargAggregator()

    //The types should not matter as the checks are made against the command function
    @Suppress("MemberVisibilityCanBePrivate")
    internal fun singleAggregator(it: Any) = it

    @Suppress("MemberVisibilityCanBePrivate")
    internal fun varargAggregator(args: List<Any>) = args
}