package com.freya02.botcommands.api.core.options.builder

import com.freya02.botcommands.internal.*
import com.freya02.botcommands.internal.utils.ReflectionUtils.reflectReference
import kotlin.reflect.KFunction
import kotlin.reflect.full.valueParameters

open class OptionAggregateBuilder internal constructor(
    /**
     * Can either be the aggregator or the command function
     *
     * See [AbstractOption.kParameter]
     */
    @get:JvmSynthetic
    internal val owner: KFunction<*>,
    @get:JvmSynthetic
    internal val declaredName: String,
    //The framework could just try to push the data it had declared in the DSL
    // using MethodHandle#invoke, transforming *at most* one array parameter with MH#asCollector
    aggregator: KFunction<*>
) {
    private val _optionBuilders: MutableMap<String, MutableList<OptionBuilder>> = mutableMapOf()
    @get:JvmSynthetic
    internal val optionBuilders: Map<String, List<OptionBuilder>>
        get() = _optionBuilders
    @get:JvmSynthetic
    internal val parameter = owner.valueParameters.first { it.findDeclarationName() == declaredName }

    @get:JvmSynthetic
    internal val aggregator: KFunction<*> = aggregator.reflectReference()

    init {
        requireUser(aggregator.returnType == parameter.type, aggregator) {
            "Aggregator should have the same return type as the parameter (${parameter.type})"
        }
    }

    @JvmSynthetic
    internal operator fun plusAssign(optionBuilder: OptionBuilder) {
        _optionBuilders.computeIfAbsent(optionBuilder.declaredName) { arrayListOf() }.add(optionBuilder)
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
