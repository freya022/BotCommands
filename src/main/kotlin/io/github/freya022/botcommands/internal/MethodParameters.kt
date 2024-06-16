package io.github.freya022.botcommands.internal

import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.options.annotations.Aggregate
import io.github.freya022.botcommands.api.core.options.builder.OptionAggregateBuilder
import io.github.freya022.botcommands.api.core.options.builder.OptionBuilder
import io.github.freya022.botcommands.internal.core.options.builder.InternalAggregators
import io.github.freya022.botcommands.internal.core.reflection.Function
import io.github.freya022.botcommands.internal.parameters.AggregatorParameter
import io.github.freya022.botcommands.internal.parameters.MethodParameterMixin
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.findDeclarationName
import io.github.freya022.botcommands.internal.utils.throwInternal
import io.github.freya022.botcommands.internal.utils.throwUser
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

internal class BasicOptionAggregateBuilder(
    aggregatorParameter: AggregatorParameter,
    aggregator: KFunction<*>
) : OptionAggregateBuilder<BasicOptionAggregateBuilder>(aggregatorParameter, aggregator) {
    override val context: BContext
        get() = throwInternal("Internal aggregate builder should not be used outside of the += operator")

    override val declarationSiteHolder: IDeclarationSiteHolder
        get() = throwInternal("Internal aggregate builder should not be used outside of the += operator")

    override fun constructNestedAggregate(aggregatorParameter: AggregatorParameter, aggregator: KFunction<*>) =
        BasicOptionAggregateBuilder(aggregatorParameter, aggregator)
}

internal inline fun <reified T : OptionAggregateBuilder<*>, R : MethodParameterMixin> Map<String, T>.transform(aggregateBlock: (T) -> R) =
    values.map(aggregateBlock)

internal fun <R : MethodParameterMixin> Function<*>.transformParameters(
    builderBlock: (function: KFunction<*>, parameter: KParameter, declaredName: String) -> OptionBuilder,
    aggregateBlock: (OptionAggregateBuilder<*>) -> R
): List<R> = kFunction.nonInstanceParameters.drop(1).associate { parameter ->
    val declaredName = parameter.findDeclarationName()
    declaredName to when {
        parameter.hasAnnotation<Aggregate>() -> {
            val constructor = parameter.type.jvmErasure.primaryConstructor
                ?: throwUser(parameter.function, "Found no constructor for aggregate type ${parameter.type}")
            BasicOptionAggregateBuilder(AggregatorParameter.fromUserAggregate(constructor, declaredName), constructor).apply {
                constructor.nonInstanceParameters.forEach { constructorParameter ->
                    this += builderBlock(constructor, constructorParameter, constructorParameter.findDeclarationName())
                }
            }
        }

        else -> BasicOptionAggregateBuilder(
            AggregatorParameter.fromSelfAggregate(parameter.function, declaredName),
            InternalAggregators.theSingleAggregator
        ).apply {
            this += builderBlock(parameter.function, parameter, declaredName)
        }
    }
}.transform(aggregateBlock)