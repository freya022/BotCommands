package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.core.reflection.isSingleAggregator
import com.freya02.botcommands.internal.core.reflection.parameters
import com.freya02.botcommands.internal.parameters.IAggregatedParameter
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.throwInternal
import com.freya02.botcommands.internal.throwUser
import com.freya02.botcommands.internal.utils.ReflectionUtils.function
import net.dv8tion.jda.api.events.Event
import kotlin.reflect.KParameter

enum class InsertOptionResult {
    OK,
    SKIP,
    ABORT
}

inline fun List<IAggregatedParameter>.mapOptions(block: MutableMap<Option, Any?>.(Option) -> Unit): Map<Option, Any?> {
    val options = this.flatMap { it.allOptions }
    return buildMap(options.size) {
        options.forEach { block(it) }
    }
}

fun tryInsertNullableOption(value: Any?, option: Option, optionMap: MutableMap<Option, Any?>): InsertOptionResult {
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
suspend fun Collection<IAggregatedParameter>.mapFinalParameters(
    event: Event,
    optionValues: Map<Option, Any?>
) = buildMap(function.parameters.size) {
    this[function.instanceParameter] = instance
    this[function.eventParameter] = event

    for (parameter in this@mapFinalParameters) {
        insertAggregate(event, this, optionValues, parameter)
    }
}

suspend fun insertAggregate(event: Event, aggregatedObjects: MutableMap<KParameter, Any?>, optionValues: Map<Option, Any?>, parameter: IAggregatedParameter) {
    val aggregator = parameter.aggregator

    if (aggregator.isSingleAggregator()) {
        val option = parameter.options.first()
        //This is necessary to distinguish between null mappings and default mappings
        if (option in optionValues) {
            //No need to check nullabilities, it's already handled when computing option values
            aggregatedObjects[parameter] = optionValues[option]
        }
    } else {
        val aggregatorArguments: MutableMap<KParameter, Any?> = HashMap(aggregator.parameters.size)
        for (option in parameter.options) {
            //This is necessary to distinguish between null mappings and default mappings
            if (option in optionValues) {
                aggregatorArguments[option] = optionValues[option]
            }
        }

        for (nestedAggregatedParameter in parameter.nestedAggregatedParameters) {
            insertAggregate(event, aggregatorArguments, optionValues, nestedAggregatedParameter)
        }

        val aggregatedObject = aggregator.aggregate(event, aggregatorArguments)
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