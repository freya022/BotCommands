package io.github.freya022.botcommands.internal.utils

import dev.freya02.botcommands.method.accessors.internal.MethodArguments
import io.github.freya022.botcommands.internal.ExecutableMixin
import io.github.freya022.botcommands.internal.core.options.OptionImpl
import io.github.freya022.botcommands.internal.parameters.AggregatedParameterMixin
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

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

context(executable: ExecutableMixin)
internal suspend fun Collection<AggregatedParameterMixin>.mapFinalParameters(
    firstParam: Any,
    optionValues: Map<out OptionImpl, Any?>
): MethodArguments {
    val args = executable.methodAccessor.createBlankArguments()
    args[0] = firstParam

    for (parameter in this@mapFinalParameters) {
        insertAggregate(firstParam, args, optionValues, parameter)
    }

    return args
}

// TODO the whole thing with parameters, aggregates and options needs to be refactored
//  This isn't even an utility, this is literally the whole logic
//  Options and aggregates are set out-of-order, this wasn't a problem when we assigned a Map<KParameter, Any?>,
//    but now that we use a glorified array to pass our arguments,
//    we are keeping track of parameter indexes, which are offset if there is an instance parameter, very ugly.
//  We should be using MethodArguments#push instead, so we don't worry about indexes,
//    however this requires arguments to be set in the right order.
private suspend fun insertAggregate(firstParam: Any, aggregatedObjects: MethodArguments, optionValues: Map<out OptionImpl, Any?>, parameter: AggregatedParameterMixin) {
    val aggregator = parameter.aggregator

    if (aggregator.isSingleAggregator) {
        val option = parameter.options.first()
        //This is necessary to distinguish between null mappings and default mappings
        if (option in optionValues) {
            //No need to check nullabilities, it's already handled when computing option values
            aggregatedObjects[parameter] = optionValues[option]
        }
    } else {
        val aggregatorArguments = aggregator.methodAccessor.createBlankArguments()
        var addedOption = false
        for (option in parameter.options) {
            //This is necessary to distinguish between null mappings and default mappings
            if (option in optionValues) {
                aggregatorArguments[option] = optionValues[option]
                addedOption = true
            }
        }
        // If this is not a vararg, it should throw later when calling the aggregator
        if (!addedOption && parameter.isVararg) {
            if (aggregator.methodAccessor.hasInstance()) {
                aggregatorArguments[aggregator.kFunction.valueParameters.last().index - 1] = emptyList<Any?>()
            } else {
                aggregatorArguments[aggregator.kFunction.valueParameters.last().index] = emptyList<Any?>()
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

private operator fun MethodArguments.set(parameter: AggregatedParameterMixin, obj: Any?): Any? = obj.also {
    if (parameter.executableParameter.function.instanceParameter != null) {
        this[parameter.executableParameter.index - 1] = obj
    } else {
        this[parameter.executableParameter.index] = obj
    }
}

@Suppress("UNCHECKED_CAST")
private operator fun MethodArguments.set(option: OptionImpl, obj: Any?) {
    val index = when {
        (option.parent as AggregatedParameterMixin).aggregator.methodAccessor.hasInstance() -> option.executableParameter.index - 1
        else -> option.executableParameter.index
    }

    if (option.isVararg) {
        val list = run {
            val obj = get(index)
            if (obj == MethodArguments.NO_VALUE)
                return@run arrayListOf<Any?>().also { set(index, it) }
            obj!! as MutableList<Any?>
        }
        list.add(obj)
    } else {
        set(index, obj)
    }
}
