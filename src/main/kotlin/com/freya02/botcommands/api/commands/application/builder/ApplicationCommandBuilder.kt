package com.freya02.botcommands.api.commands.application.builder

import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import com.freya02.botcommands.api.commands.builder.CommandBuilder
import com.freya02.botcommands.api.commands.builder.IBuilderFunctionHolder
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

abstract class ApplicationCommandBuilder<T : ApplicationCommandOptionAggregateBuilder> internal constructor(
    name: String,
    function: KFunction<Any>
) : CommandBuilder(name), IBuilderFunctionHolder<Any> {
    final override val function = function.reflectReference()

    abstract val topLevelBuilder: ITopLevelApplicationCommandBuilder

    var defaultLocked: Boolean = DEFAULT_DEFAULT_LOCKED

    var nsfw: Boolean = false

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun customOption(declaredName: String) {
        selfAggregate(declaredName) {
            customOption(declaredName)
        }
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    fun generatedOption(declaredName: String, generatedValueSupplier: ApplicationGeneratedValueSupplier) {
        selfAggregate(declaredName) {
            generatedOption(declaredName, generatedValueSupplier)
        }
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun aggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit = {}) {
        aggregate(declaredName, aggregator, aggregator, block)
    }

    protected fun selfAggregate(declaredName: String, block: T.() -> Unit) {
        //When the option needs to be searched on the command function instead of the aggregator
        aggregate(declaredName, function, ::singleAggregator, block)
    }

    private fun aggregate(declaredName: String, owner: KFunction<*>, aggregator: KFunction<*>, block: T.() -> Unit) {
        optionAggregateBuilders[declaredName] = constructAggregate(declaredName, owner, aggregator).apply(block)
    }

    protected abstract fun constructAggregate(declaredName: String, owner: KFunction<*>, aggregator: KFunction<*>): T

    companion object {
        const val DEFAULT_DEFAULT_LOCKED = false

        //The types should not matter as the checks are made against the command function
        fun singleAggregator(it: Any) = it
    }
}
