package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.internal.ExecutableMixin
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.core.reflection.buildParameters
import io.github.freya022.botcommands.internal.parameters.AggregatedParameterMixin
import io.github.freya022.botcommands.internal.parameters.MethodParameterMixin
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import kotlin.reflect.KParameter

internal enum class InsertOptionResult {
    OK,
    SKIP,
    ABORT
}

internal inline fun List<AggregatedParameterMixin>.mapOptions(block: MutableMap<OptionImpl, Any?>.(OptionImpl) -> Unit): Map<OptionImpl, Any?> {
    val options = this.flatMap { it.allOptions }
    return buildMap(options.size) {
        options.forEach { block(it) }
    }
}

internal fun tryInsertNullableOption(value: Any?, option: OptionImpl, optionMap: MutableMap<OptionImpl, Any?>): InsertOptionResult {
    if (value != null) {
        optionMap[option] = value
        return InsertOptionResult.OK
    } else if (option.isVararg) {
        //Continue looking at other options
    } else if (option.isOptionalOrNullable) { //Default or nullable
        //Put null/default value if parameter is not a kotlin default value
        if (option.isOptional) {
            //Kotlin default value, don't add anything to the parameters map
        } else {
            //Nullable
            optionMap[option] = option.nullValue
        }
    } else {
        //Value is null and is required
        throwArgument(option.typeCheckingFunction, "Option #${option.index} (${option.declaredName}) couldn't be resolved")
    }

    return InsertOptionResult.SKIP
}

context(ExecutableMixin)
internal suspend fun Collection<AggregatedParameterMixin>.mapFinalParameters(
    firstParam: Any,
    optionValues: Map<out OptionImpl, Any?>
) = buildParameters(eventFunction.kFunction) {
    this[eventFunction.instanceParameter] = instance
    this[eventFunction.firstParameter] = firstParam

    for (parameter in this@mapFinalParameters) {
        insertAggregate(firstParam, this, optionValues, parameter)
    }
}

private suspend fun insertAggregate(firstParam: Any, aggregatedObjects: MutableMap<KParameter, Any?>, optionValues: Map<out OptionImpl, Any?>, parameter: AggregatedParameterMixin) {
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
            if (parameter.isNullable) {
                aggregatedObjects[parameter] = null
            } else if (parameter.isOptional) {
                // Don't associate parameter to a value
            } else {
                throwArgument(parameter.executableParameter.function, "Aggregated parameter couldn't be resolved at option ${parameter.name}")
            }
        }
    }
}

private operator fun MutableMap<KParameter, Any?>.set(parameter: MethodParameterMixin, obj: Any?): Any? = obj.also {
    this[parameter.executableParameter] = obj
}

@Suppress("UNCHECKED_CAST")
private operator fun MutableMap<KParameter, Any?>.set(option: OptionImpl, obj: Any?): Any? = obj.also {
    if (option.isVararg) {
        (this.getOrPut(option.executableParameter) {
            arrayListOf<Any?>()
        } as MutableList<Any?>).add(obj)
    } else {
        this[option.executableParameter] = obj
    }
}