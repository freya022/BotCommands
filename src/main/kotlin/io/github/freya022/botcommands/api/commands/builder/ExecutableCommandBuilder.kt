package io.github.freya022.botcommands.api.commands.builder

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.api.core.options.builder.OptionRegistry
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderContainerMixin
import io.github.freya022.botcommands.internal.core.options.builder.OptionAggregateBuilderContainerMixinImpl
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

abstract class ExecutableCommandBuilder<T : OptionAggregateBuilder<T>, R> internal constructor(
    context: BContext,
    name: String,
    function: KFunction<R>
) : CommandBuilder(context, name), IBuilderFunctionHolder<R>,
    /* Can't delegate as this requires a `this` reference */
    OptionAggregateBuilderContainerMixin<T>,
    OptionRegistry<T> {

    private val aggregateContainer = OptionAggregateBuilderContainerMixinImpl(function, ::constructAggregate)

    final override val function: KFunction<R> = function.reflectReference()

    override val optionAggregateBuilders: Map<String, T> get() = aggregateContainer.optionAggregateBuilders

    override fun hasVararg(): Boolean = aggregateContainer.hasVararg()
    override fun serviceOption(declaredName: String) {
        selfAggregate(declaredName) {
            serviceOption(declaredName)
        }
    }

    override fun customOption(declaredName: String) {
        selfAggregate(declaredName) {
            customOption(declaredName)
        }
    }

    override fun aggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit) =
        aggregateContainer.aggregate(declaredName, aggregator, block)

    override fun selfAggregate(declaredName: String, block: T.() -> Unit) =
        aggregateContainer.selfAggregate(declaredName, block)

    override fun varargAggregate(declaredName: String, block: T.() -> Unit) =
        aggregateContainer.varargAggregate(declaredName, block)

    internal abstract fun constructAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>): T
}
