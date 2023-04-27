package com.freya02.botcommands.api.commands.builder

import com.freya02.botcommands.api.commands.CommandOptionBuilder
import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.impl.core.options.builder.OptionAggregateBuildersImpl
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import java.util.*
import kotlin.reflect.KFunction

abstract class ExecutableCommandBuilder<T : OptionAggregateBuilder, R> internal constructor(
    name: String,
    function: KFunction<R>
) : CommandBuilder(name), IBuilderFunctionHolder<R> {
    final override val function: KFunction<R> = function.reflectReference()

    @Deprecated("Replaced with optionAggregateBuilders")
    @get:JvmSynthetic
    internal val commandOptionBuilders: MutableMap<String, CommandOptionBuilder> = mutableMapOf()

    private val _optionAggregateBuilders = OptionAggregateBuildersImpl(function, ::constructAggregate)

    @get:JvmSynthetic
    internal val optionAggregateBuilders: MutableMap<String, OptionAggregateBuilder>
        get() = _optionAggregateBuilders.optionAggregateBuilders

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun aggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit = {}) {
        _optionAggregateBuilders.aggregate(declaredName, aggregator, block)
    }

    protected fun selfAggregate(declaredName: String, block: T.() -> Unit) {
        _optionAggregateBuilders.selfAggregate(declaredName, block)
    }

    protected abstract fun constructAggregate(declaredName: String, owner: KFunction<*>, aggregator: KFunction<*>): T
}
