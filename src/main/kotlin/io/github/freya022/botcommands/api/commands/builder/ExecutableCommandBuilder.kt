package io.github.freya022.botcommands.api.commands.builder

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.api.core.service.annotations.Condition
import io.github.freya022.botcommands.api.core.service.annotations.ConditionalService
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

abstract class ExecutableCommandBuilder<T : OptionAggregateBuilder<T>, R> internal constructor(
    context: BContext,
    name: String,
    function: KFunction<R>
) : CommandBuilder(context, name), IBuilderFunctionHolder<R> {
    final override val function: KFunction<R> = function.reflectReference()

    private val _optionAggregateBuilders = OptionAggregateBuildersImpl(function, ::constructAggregate)

    internal val optionAggregateBuilders: Map<String, T>
        get() = _optionAggregateBuilders.optionAggregateBuilders

    /**
     * Declares a service option, allowing injection of services, which must be available.
     *
     * If the service is not available, then either don't declare this command,
     * or make the declaring class disabled by using one of:
     * - [@Condition][Condition]
     * - [@ConditionalService][ConditionalService]
     * - [@Dependencies][Dependencies]
     *
     * @param declaredName Name of the declared parameter in the [command function][function]
     */
    fun serviceOption(declaredName: String) {
        selfAggregate(declaredName) {
            serviceOption(declaredName)
        }
    }

    /**
     * Declares multiple options aggregated in a single parameter.
     *
     * The aggregator will receive all the options in the declared order and produce a single output.
     *
     * @param declaredName Name of the declared parameter in the [command function][function]
     * @param aggregator   The function taking all the options and merging them in a single output
     *
     * @see Aggregate @Aggregate
     */
    fun aggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit = {}) {
        _optionAggregateBuilders.aggregate(declaredName, aggregator, block)
    }

    protected fun selfAggregate(declaredName: String, block: T.() -> Unit) {
        _optionAggregateBuilders.selfAggregate(declaredName, block)
    }

    protected fun varargAggregate(declaredName: String, block: T.() -> Unit) {
        _optionAggregateBuilders.varargAggregate(declaredName, block)
    }

    internal abstract fun constructAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>): T
}
