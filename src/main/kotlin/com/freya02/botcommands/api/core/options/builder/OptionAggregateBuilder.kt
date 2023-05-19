package com.freya02.botcommands.api.core.options.builder

import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl
import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl.Companion.isSpecialAggregator
import com.freya02.botcommands.internal.parameters.AggregatorParameter
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

abstract class OptionAggregateBuilder<T : OptionAggregateBuilder<T>> internal constructor(
    val aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) {
    @get:JvmSynthetic
    internal val aggregator: KFunction<*> = aggregator.reflectReference()
    @get:JvmSynthetic
    internal val parameter = aggregatorParameter.typeCheckingParameter

    private val _optionBuilders: MutableMap<String, MutableList<OptionBuilder>> = mutableMapOf()
    @get:JvmSynthetic
    internal val optionBuilders: Map<String, List<OptionBuilder>>
        get() = _optionBuilders

    private val _nestedAggregates = OptionAggregateBuildersImpl(aggregator, ::constructNestedAggregate)
    @get:JvmSynthetic
    internal val nestedAggregates: Map<String, T>
        get() = _nestedAggregates.optionAggregateBuilders

    init {
        //Do not check return type of trusted aggregators
        requireUser(aggregator.isSpecialAggregator() || aggregator.returnType == aggregatorParameter.typeCheckingParameter.type, aggregator) {
            "Aggregator should have the same return type as the parameter (required: ${aggregatorParameter.typeCheckingParameter.type}, found: ${aggregator.returnType})"
        }
    }

    fun nestedAggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit) {
        _nestedAggregates.aggregate(declaredName, aggregator, block)
    }

    protected fun nestedVarargAggregate(declaredName: String, block: T.() -> Unit) {
        _nestedAggregates.varargAggregate(declaredName, block)
    }

    //TODO should add self/vararg ?

    protected abstract fun constructNestedAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>): T

    @JvmSynthetic
    internal operator fun plusAssign(optionBuilder: OptionBuilder) {
        _optionBuilders.computeIfAbsent(optionBuilder.optionParameter.typeCheckingParameterName) { arrayListOf() }.add(optionBuilder)
    }
}
