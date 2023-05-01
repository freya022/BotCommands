package com.freya02.botcommands.internal

import com.freya02.botcommands.api.core.options.annotations.Aggregate
import com.freya02.botcommands.api.core.options.builder.OptionAggregateBuilder
import com.freya02.botcommands.api.core.options.builder.OptionBuilder
import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl
import com.freya02.botcommands.internal.parameters.AggregatorParameter
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.jvmErasure

@Deprecated("Will be replaced with a generic counterpart")
typealias MethodParameters = List<MethodParameter>

internal inline fun <reified T : OptionAggregateBuilder, R> Map<String, OptionAggregateBuilder>.transform(aggregateBlock: (T) -> R) =
    values.map {
        if (it !is T)
            throwInternal("Aggregates should have consisted of ${T::class.simpleNestedName} instances")
        aggregateBlock(it)
    }

internal fun <R> List<KParameter>.transformParameters(
    builderBlock: (function: KFunction<*>, parameter: KParameter, declaredName: String) -> OptionBuilder,
    aggregateBlock: (OptionAggregateBuilder) -> R
) = associate { parameter ->
    val declaredName = parameter.findDeclarationName()
    declaredName to when {
        parameter.hasAnnotation<Aggregate>() -> {
            val constructor = parameter.type.jvmErasure.primaryConstructor
                ?: throwUser(parameter.function, "Found no constructor for aggregate type ${parameter.type}")
            OptionAggregateBuilder(AggregatorParameter.fromUserAggregate(constructor, declaredName), constructor).apply {
                constructor.nonInstanceParameters.forEach { constructorParameter ->
                    this += builderBlock(constructor, constructorParameter, constructorParameter.findDeclarationName())
                }
            }
        }

        else -> OptionAggregateBuilder(
            AggregatorParameter.fromSelfAggregate(parameter.function, declaredName),
            OptionAggregateBuildersImpl.theSingleAggregator
        ).apply {
            this += builderBlock(parameter.function, parameter, declaredName)
        }
    }
}.transform(aggregateBlock)