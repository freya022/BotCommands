package com.freya02.botcommands.internal.core.options.builder

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.internal.parameters.MultiParameter
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

internal class OptionAggregateBuildersImpl<T : OptionAggregateBuilder>(
    private val commandFunction: KFunction<*>,
    val aggregateConstructor: (multiParameter: MultiParameter, aggregator: KFunction<*>) -> T
) {
    val optionAggregateBuilders: MutableMap<String, OptionAggregateBuilder> = hashMapOf()

    fun aggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit) {
        aggregate(MultiParameter.fromUserAggregate(commandFunction, declaredName), aggregator, block)
    }

    fun selfAggregate(declaredName: String, block: T.() -> Unit) {
        //When the option needs to be searched on the command function instead of the aggregator
        aggregate(MultiParameter.fromSelfAggregate(commandFunction, declaredName), theSingleAggregator, block)
    }

    private fun aggregate(multiParameter: MultiParameter, aggregator: KFunction<*>, block: T.() -> Unit) {
        optionAggregateBuilders[multiParameter.typeCheckingParameterName] = aggregateConstructor(multiParameter, aggregator).apply(block)
    }

    @BService
    companion object {
        val theSingleAggregator = Companion::singleAggregator.reflectReference()

        fun KFunction<*>.isSingleAggregator() = this === theSingleAggregator

        //The types should not matter as the checks are made against the command function
        @Suppress("UNUSED_PARAMETER", "MemberVisibilityCanBePrivate")
        fun singleAggregator(event: Any, it: Any) = it
    }
}