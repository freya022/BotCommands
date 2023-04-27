package com.freya02.botcommands.internal.core.options.builder

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import kotlin.reflect.KFunction

//TODO fix package
internal class OptionAggregateBuildersImpl<T : OptionAggregateBuilder>(
    private val owner: KFunction<*>,
    val aggregateConstructor: (declaredName: String, owner: KFunction<*>, aggregator: KFunction<*>) -> T
) {
    val optionAggregateBuilders: MutableMap<String, OptionAggregateBuilder> = hashMapOf()

    fun aggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit) {
        aggregate(declaredName, aggregator, aggregator, block)
    }

    fun selfAggregate(declaredName: String, block: T.() -> Unit) {
        //When the option needs to be searched on the command function instead of the aggregator
        aggregate(declaredName, owner, Companion::singleAggregator, block)
    }

    private fun aggregate(declaredName: String, owner: KFunction<*>, aggregator: KFunction<*>, block: T.() -> Unit) {
        optionAggregateBuilders[declaredName] = aggregateConstructor(declaredName, owner, aggregator).apply(block)
    }

    @BService
    companion object {
        //The types should not matter as the checks are made against the command function
        fun singleAggregator(it: Any) = it
    }
}