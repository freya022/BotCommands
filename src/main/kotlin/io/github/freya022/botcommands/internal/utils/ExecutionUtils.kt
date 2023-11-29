package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.internal.IExecutableInteractionInfo
import io.github.freya022.botcommands.internal.core.options.Option
import io.github.freya022.botcommands.internal.parameters.IAggregatedParameter
import io.github.freya022.botcommands.internal.parameters.MethodParameter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import kotlin.reflect.KParameter

internal enum class InsertOptionResult {
    OK,
    SKIP,
    ABORT
}

internal inline fun List<IAggregatedParameter>.mapOptions(block: MutableMap<Option, Any?>.(Option) -> Unit): Map<Option, Any?> {
    val options = this.flatMap { it.allOptions }
    return buildMap(options.size) {
        options.forEach { block(it) }
    }
}

internal fun tryInsertNullableOption(value: Any?, option: Option, optionMap: MutableMap<Option, Any?>): InsertOptionResult {
    if (value != null) {
        optionMap[option] = value
        return InsertOptionResult.OK
    } else if (option.isVararg) {
        //Continue looking at other options
    } else if (option.isOptionalOrNullable) { //Default or nullable
        //Put null/default value if parameter is not a kotlin default value
        if (option.kParameter.isOptional) {
            //Kotlin default value, don't add anything to the parameters map
        } else {
            //Nullable
            optionMap[option] = option.nullValue
        }
    } else {
        //Value is null and is required
        throwUser(option.optionParameter.typeCheckingFunction, "Option #${option.index} (${option.declaredName}) couldn't be resolved, this could be due to a faulty ICustomResolver")
    }

    return InsertOptionResult.SKIP
}

context(IExecutableInteractionInfo)
internal suspend fun Collection<IAggregatedParameter>.mapFinalParameters(
    firstParam: Any,
    optionValues: Map<Option, Any?>
) = buildMap(eventFunction.parametersSize) {
    this[eventFunction.instanceParameter] = instance
    this[eventFunction.firstParameter] = firstParam

    for (parameter in this@mapFinalParameters) {
        insertAggregate(firstParam, this, optionValues, parameter)
    }
}

internal suspend fun insertAggregate(firstParam: Any, aggregatedObjects: MutableMap<KParameter, Any?>, optionValues: Map<Option, Any?>, parameter: IAggregatedParameter) {
    val aggregator = parameter.aggregator

    if (aggregator.isSingleAggregator) {
        val option = parameter.options.first()
        //This is necessary to distinguish between null mappings and default mappings
        if (option in optionValues) {
            //No need to check nullabilities, it's already handled when computing option values
            aggregatedObjects[parameter] = optionValues[option]
        }
    } else {
        val aggregatorArguments: MutableMap<KParameter, Any?> = HashMap(aggregator.parametersSize)
        for (option in parameter.options) {
            //This is necessary to distinguish between null mappings and default mappings
            if (option in optionValues) {
                aggregatorArguments[option] = optionValues[option]
            }
        }

        for (nestedAggregatedParameter in parameter.nestedAggregatedParameters) {
            insertAggregate(firstParam, aggregatorArguments, optionValues, nestedAggregatedParameter)
        }

        val aggregatedObject = aggregator.aggregate(firstParam, aggregatorArguments)
        //Check nullability against parameter
        if (aggregatedObject != null) {
            aggregatedObjects[parameter] = aggregatedObject
        } else {
            if (parameter.isNullableOrOptional) { //Default or nullable
                //Put null/default value if parameter is not a kotlin default value
                return if (parameter.kParameter.isOptional) {
                    //Kotlin default value, don't add anything to the parameters map
                } else {
                    //Nullable
                    aggregatedObjects[parameter] = when {
                        parameter.isPrimitive -> throwInternal("Cannot have user-defined aggregators returning primitives")
                        else -> null
                    }
                }
            } else {
                throwUser(parameter.executableParameter.function, "Aggregated parameter couldn't be resolved at option ${parameter.name}")
            }
        }
    }
}

private operator fun MutableMap<KParameter, Any?>.set(parameter: MethodParameter, obj: Any?): Any? = obj.also {
    this[parameter.executableParameter] = obj
}

@Suppress("UNCHECKED_CAST")
private operator fun MutableMap<KParameter, Any?>.set(option: Option, obj: Any?): Any? = obj.also {
    if (option.isVararg) {
        (this.getOrPut(option.executableParameter) {
            arrayListOf<Any?>()
        } as MutableList<Any?>).add(obj)
    } else {
        this[option.executableParameter] = obj
    }
}