package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.internal.IExecutableInteractionInfo
import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl.Companion.isSingleAggregator
import com.freya02.botcommands.internal.parameters.IAggregatedParameter
import com.freya02.botcommands.internal.parameters.MethodParameter
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import net.dv8tion.jda.api.events.Event
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

enum class InsertOptionResult {
    OK,
    SKIP,
    ABORT
}

operator fun MutableMap<KParameter, Any?>.set(parameter: MethodParameter, obj: Any?): Any? = obj.also {
    this[parameter.kParameter] = obj
}

@Suppress("UNCHECKED_CAST")
operator fun MutableMap<KParameter, Any?>.set(option: Option, obj: Any?): Any? = obj.also {
    if (option.isVararg) {
        (this.getOrPut(option.executableParameter) {
            arrayListOf<Any?>()
        } as MutableList<Any?>).add(obj)
    } else {
        this[option.executableParameter] = obj
    }
}

context(IExecutableInteractionInfo)
suspend fun Collection<IAggregatedParameter>.mapFinalParameters(event: Event, optionValues: Map<Option, Any?>): Map<KParameter, Any?> {
    val aggregatedObjects: MutableMap<KParameter, Any?> = hashMapOf()
    aggregatedObjects[method.instanceParameter!!] = instance
    aggregatedObjects[method.nonInstanceParameters.first()] = event

    for (parameter in this) {
        insertAggregate(event, aggregatedObjects, optionValues, parameter)
    }

    return aggregatedObjects
}

suspend fun insertAggregate(event: Event, aggregatedObjects: MutableMap<KParameter, Any?>, optionValues: Map<Option, Any?>, parameter: IAggregatedParameter) {
    val aggregator = parameter.aggregator
    if (aggregator.isSingleAggregator()) {
        val option = parameter.commandOptions.first()
        //This is necessary to distinguish between null mappings and default mappings
        if (option in optionValues) {
            aggregatedObjects[parameter] = optionValues[option]
        }
    } else {
        val aggregatorArguments: MutableMap<KParameter, Any?> = HashMap(aggregator.parameters.size)
        aggregatorArguments[aggregator.instanceParameter!!] = parameter.aggregatorInstance
        aggregatorArguments[aggregator.valueParameters.first()] = event

        for (option in parameter.commandOptions) {
            //This is necessary to distinguish between null mappings and default mappings
            if (option in optionValues) {
                aggregatorArguments[option] = optionValues[option]
            }
        }

        aggregatedObjects[parameter] = aggregator.callSuspendBy(aggregatorArguments)
    }
}