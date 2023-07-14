package com.freya02.botcommands.api.commands.builder

import com.freya02.botcommands.api.core.options.annotations.Aggregate
import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl
import com.freya02.botcommands.internal.parameters.AggregatorParameter
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

abstract class ExecutableCommandBuilder<T : OptionAggregateBuilder<T>, R> internal constructor(
    name: String,
    function: KFunction<R>
) : CommandBuilder(name), IBuilderFunctionHolder<R> {
    final override val function: KFunction<R> = function.reflectReference()

    private val _optionAggregateBuilders = OptionAggregateBuildersImpl(function, ::constructAggregate)

    internal val optionAggregateBuilders: Map<String, T>
        get() = _optionAggregateBuilders.optionAggregateBuilders

    /**
     * @param declaredName Name of the declared parameter in the [function]
     *
     * @see Aggregate
     */
    @JvmOverloads
    fun aggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit = {}) {
        _optionAggregateBuilders.aggregate(declaredName, aggregator, block)
    }

    protected fun selfAggregate(declaredName: String, block: T.() -> Unit) {
        _optionAggregateBuilders.selfAggregate(declaredName, block)
    }

    protected fun varargAggregate(declaredName: String, block: T.() -> Unit) {
        _optionAggregateBuilders.varargAggregate(declaredName, block)
    }

    protected abstract fun constructAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>): T
}
