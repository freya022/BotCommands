package com.freya02.botcommands.internal.core.options.builder

import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.internal.annotations.IncludeClasspath
import com.freya02.botcommands.internal.parameters.AggregatorParameter
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

internal class OptionAggregateBuildersImpl<T : OptionAggregateBuilder>(
    private val commandFunction: KFunction<*>,
    val aggregateConstructor: (aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) -> T
) {
    val optionAggregateBuilders: MutableMap<String, OptionAggregateBuilder> = hashMapOf()

    fun aggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit) {
        aggregate(AggregatorParameter.fromUserAggregate(commandFunction, declaredName), aggregator, block)
    }

    fun selfAggregate(declaredName: String, block: T.() -> Unit) {
        //When the option needs to be searched on the command function instead of the aggregator
        aggregate(AggregatorParameter.fromSelfAggregate(commandFunction, declaredName), theSingleAggregator, block)
    }

    private fun aggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>, block: T.() -> Unit) {
        optionAggregateBuilders[aggregatorParameter.typeCheckingParameterName] = aggregateConstructor(aggregatorParameter, aggregator).apply(block)
    }

    @IncludeClasspath
    companion object {
        val theSingleAggregator = Companion::singleAggregator.reflectReference()

        fun KFunction<*>.isSingleAggregator() = this === theSingleAggregator

        //The types should not matter as the checks are made against the command function
        @Suppress("UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")
        fun singleAggregator(event: Any, it: Any) = it
    }
}