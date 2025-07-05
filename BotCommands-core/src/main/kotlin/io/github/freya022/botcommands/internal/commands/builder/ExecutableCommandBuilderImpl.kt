package io.github.freya022.botcommands.internal.commands.builder

import io.github.freya022.botcommands.api.commands.builder.ExecutableCommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderContainerMixin
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderContainerMixinImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

internal abstract class ExecutableCommandBuilderImpl<T : OptionAggregateBuilder<T>, R> internal constructor(
    context: BContext,
    name: String,
    function: KFunction<R>
) : CommandBuilderImpl(context, name),
    ExecutableCommandBuilder<T>,
    IBuilderFunctionHolder<R>,
    /* Can't delegate as this requires a `this` reference */
    OptionAggregateBuilderContainerMixin<T> {

    private val aggregateContainer = OptionAggregateBuilderContainerMixinImpl(function, ::constructAggregate)

    final override val function: KFunction<R> = function.reflectReference()

    final override val optionAggregateBuilders: Map<String, T> get() = aggregateContainer.optionAggregateBuilders

    final override fun hasVararg(): Boolean = aggregateContainer.hasVararg()

    final override fun serviceOption(declaredName: String) {
        selfAggregate(declaredName) {
            serviceOption(declaredName)
        }
    }

    final override fun customOption(declaredName: String) {
        selfAggregate(declaredName) {
            customOption(declaredName)
        }
    }

    final override fun aggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit) =
        aggregateContainer.aggregate(declaredName, aggregator, block)

    final override fun selfAggregate(declaredName: String, block: T.() -> Unit) =
        aggregateContainer.selfAggregate(declaredName, block)

    final override fun varargAggregate(declaredName: String, block: T.() -> Unit) =
        aggregateContainer.varargAggregate(declaredName, block)

    internal abstract fun constructAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>): T
}