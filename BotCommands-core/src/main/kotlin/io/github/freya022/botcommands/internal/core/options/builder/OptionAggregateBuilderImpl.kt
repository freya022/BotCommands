package io.github.freya022.botcommands.internal.core.options.builder

import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators.isSpecialAggregator
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import io.github.freya022.botcommands.internal.utils.requireAt
import kotlin.reflect.KFunction

abstract class OptionAggregateBuilderImpl<T : OptionAggregateBuilder<T>>(
    val aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>,
) : OptionAggregateBuilderContainerMixin<T>,
    OptionAggregateBuilder<T> {

    val aggregator: KFunction<*> = aggregator.reflectReference()
    val parameter = aggregatorParameter.typeCheckingParameter

    private val _optionBuilders: MutableMap<String, MutableList<OptionBuilderImpl>> = mutableMapOf()
    val optionBuilders: Map<String, List<OptionBuilderImpl>>
        get() = _optionBuilders

    private val aggregateContainer = OptionAggregateBuilderContainerMixinImpl(aggregator, ::constructNestedAggregate)
    final override val optionAggregateBuilders: Map<String, T> get() = aggregateContainer.optionAggregateBuilders

    init {
        //Do not check return type of trusted aggregators
        requireAt(
            aggregator.isSpecialAggregator() || aggregator.returnType == aggregatorParameter.typeCheckingParameter.type,
            aggregator
        ) {
            "Aggregator should have the same return type as the parameter (required: ${aggregatorParameter.typeCheckingParameter.type}, found: ${aggregator.returnType})"
        }
    }

    final override fun hasVararg(): Boolean = aggregateContainer.hasVararg()

    final override fun serviceOption(declaredName: String) {
        this += ServiceOptionBuilderImpl(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    final override fun customOption(declaredName: String) {
        this += CustomOptionBuilderImpl(aggregatorParameter.toOptionParameter(aggregator, declaredName))
    }

    final override fun selfAggregate(declaredName: String, block: T.() -> Unit) =
        aggregateContainer.selfAggregate(declaredName, block)

    final override fun varargAggregate(declaredName: String, block: T.() -> Unit) =
        aggregateContainer.varargAggregate(declaredName, block)

    final override fun aggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit) =
        aggregateContainer.aggregate(declaredName, aggregator, block)

    protected abstract fun constructNestedAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>): T

    operator fun plusAssign(optionBuilder: OptionBuilderImpl) {
        _optionBuilders.computeIfAbsent(optionBuilder.optionParameter.typeCheckingParameterName) { arrayListOf() }.add(optionBuilder)
    }
}
