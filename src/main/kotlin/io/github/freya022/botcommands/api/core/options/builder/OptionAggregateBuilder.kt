package io.github.freya022.botcommands.api.core.options.builder

import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.internal.commands.CommandDSL
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isSpecialAggregator
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.requireUser
import kotlin.reflect.KFunction

@CommandDSL
abstract class OptionAggregateBuilder<T : OptionAggregateBuilder<T>> internal constructor(
    val aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) {
    internal val aggregator: KFunction<*> = aggregator.reflectReference()
    internal val parameter = aggregatorParameter.typeCheckingParameter

    private val _optionBuilders: MutableMap<String, MutableList<OptionBuilder>> = mutableMapOf()
    internal val optionBuilders: Map<String, List<OptionBuilder>>
        get() = _optionBuilders

    private val _nestedAggregates = OptionAggregateBuildersImpl(aggregator, ::constructNestedAggregate)
    internal val nestedAggregates: Map<String, T>
        get() = _nestedAggregates.optionAggregateBuilders

    init {
        //Do not check return type of trusted aggregators
        requireUser(aggregator.isSpecialAggregator() || aggregator.returnType == aggregatorParameter.typeCheckingParameter.type, aggregator) {
            "Aggregator should have the same return type as the parameter (required: ${aggregatorParameter.typeCheckingParameter.type}, found: ${aggregator.returnType})"
        }
    }

    /**
     * @see Aggregate @Aggregate
     */
    fun nestedAggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit) {
        _nestedAggregates.aggregate(declaredName, aggregator, block)
    }

    protected fun nestedVarargAggregate(declaredName: String, block: T.() -> Unit) {
        _nestedAggregates.varargAggregate(declaredName, block)
    }

    protected abstract fun constructNestedAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>): T

    internal operator fun plusAssign(optionBuilder: OptionBuilder) {
        _optionBuilders.computeIfAbsent(optionBuilder.optionParameter.typeCheckingParameterName) { arrayListOf() }.add(optionBuilder)
    }
}
