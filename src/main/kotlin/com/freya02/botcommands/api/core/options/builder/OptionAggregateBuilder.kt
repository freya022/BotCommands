package com.freya02.botcommands.api.core.options.builder

import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl.Companion.isSingleAggregator
import com.freya02.botcommands.internal.joinWithQuote
import com.freya02.botcommands.internal.parameters.MultiParameter
import com.freya02.botcommands.internal.requireUser
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction

open class OptionAggregateBuilder internal constructor(
    val multiParameter: MultiParameter,
    //The framework could just try to push the data it had declared in the DSL
    // using MethodHandle#invoke, transforming *at most* one array parameter with MH#asCollector
    aggregator: KFunction<*>
) {
    val declaredName = multiParameter.typeCheckingParameterName

    private val _optionBuilders: MutableMap<String, MutableList<OptionBuilder>> = mutableMapOf()
    @get:JvmSynthetic
    internal val optionBuilders: Map<String, List<OptionBuilder>>
        get() = _optionBuilders
    @get:JvmSynthetic
    internal val parameter = multiParameter.typeCheckingParameter

    @get:JvmSynthetic
    internal val aggregator: KFunction<*> = aggregator.reflectReference()

    init {
        requireUser(aggregator.isSingleAggregator() || aggregator.returnType == multiParameter.typeCheckingParameter.type, aggregator) {
            "Aggregator should have the same return type as the parameter (required: ${multiParameter.typeCheckingParameter.type}, found: ${aggregator.returnType})"
        }
    }

    @JvmSynthetic
    internal operator fun plusAssign(optionBuilder: OptionBuilder) {
        _optionBuilders.computeIfAbsent(optionBuilder.multiParameter.typeCheckingParameterName) { arrayListOf() }.add(optionBuilder)
    }

    companion object {
        internal inline fun <reified T : OptionAggregateBuilder> Map<String, OptionAggregateBuilder>.findOption(name: String, builderDescription: String): T {
            when (val builder = this[name]) {
                is T -> return builder
                null -> throwUser("Option '$name' was not found in the command declaration, declared options: ${this.keys.joinWithQuote()}")
                else -> throwUser("Option '$name' was found in the command declaration, but $builderDescription was expected (you may have forgotten an annotation, if you are using them)")
            }
        }
    }
}
