package com.freya02.botcommands.api.commands.prefixed.builder

import com.freya02.botcommands.api.commands.builder.IBuilderFunctionHolder
import com.freya02.botcommands.api.commands.prefixed.TextGeneratedValueSupplier
import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.impl.core.options.builder.OptionAggregateBuildersImpl
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.asDiscordString
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandVariation
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

class TextCommandVariationBuilder internal constructor(
    private val context: BContextImpl,
    function: KFunction<Any>
) : IBuilderFunctionHolder<Any> {
    override val function: KFunction<Any> = function.reflectReference()

    private val _optionAggregateBuilders = OptionAggregateBuildersImpl(function) { declaredName, owner, aggregator ->
        TextCommandOptionAggregateBuilder(owner, declaredName, aggregator)
    }

    @get:JvmSynthetic
    internal val optionAggregateBuilders: MutableMap<String, OptionAggregateBuilder>
        get() = _optionAggregateBuilders.optionAggregateBuilders

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun option(declaredName: String, optionName: String = declaredName.asDiscordString(), block: TextCommandOptionBuilder.() -> Unit = {}) {
        selfAggregate(declaredName) {
            option(declaredName, optionName, block)
        }
    }

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
    fun generatedOption(declaredName: String, generatedValueSupplier: TextGeneratedValueSupplier) {
        selfAggregate(declaredName) {
            generatedOption(declaredName, generatedValueSupplier)
        }
    }

    /**
     * @param declaredName Name of the declared parameter in the [function]
     */
    @JvmOverloads
    fun aggregate(declaredName: String, aggregator: KFunction<*>, block: TextCommandOptionAggregateBuilder.() -> Unit = {}) {
        _optionAggregateBuilders.aggregate(declaredName, aggregator, block)
    }

    private fun selfAggregate(declaredName: String, block: TextCommandOptionAggregateBuilder.() -> Unit) {
        _optionAggregateBuilders.selfAggregate(declaredName, block)
    }

    @JvmSynthetic
    internal fun build(info: TextCommandInfo): TextCommandVariation {
        return TextCommandVariation(context, info, this)
    }
}
