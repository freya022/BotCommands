package io.github.freya022.botcommands.api.core.options.builder

import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.throwArgument
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.primaryConstructor

interface OptionAggregateBuilderContainer<T : OptionAggregateBuilder<T>> {
    /**
     * Declares multiple options aggregated in a single parameter.
     *
     * The aggregator will receive all the options in the declared order and produce a single output.
     *
     * @param declaredName Name of the declared parameter which receives the value of the combined options
     * @param aggregator   The function taking all the options and merging them in a single output
     *
     * @see Aggregate @Aggregate
     */
    fun aggregate(declaredName: String, aggregator: KFunction<*>, block: T.() -> Unit = {})
}

fun <T : OptionAggregateBuilder<T>> OptionAggregateBuilderContainer<T>.inlineClassAggregate(
    declaredName: String,
    clazz: KClass<*>,
    block: T.(valueName: String) -> Unit = {},
) {
    val aggregatorConstructor = clazz.primaryConstructor
        ?: throwArgument("Found no public constructor for class ${clazz.simpleNestedName}")
    aggregate(declaredName, aggregatorConstructor) {
        val parameterName = aggregatorConstructor.parameters.singleOrNull()?.findDeclarationName()
            ?: throwArgument(aggregatorConstructor, "Constructor must only have one parameter")
        block(parameterName)
    }
}