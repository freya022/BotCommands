package com.freya02.botcommands.internal.utils

import com.freya02.botcommands.internal.commands.CommandParameter
import com.freya02.botcommands.internal.core.options.Option
import com.freya02.botcommands.internal.core.options.builder.OptionAggregateBuildersImpl.Companion.isSingleAggregator
import com.freya02.botcommands.internal.parameters.MethodParameter
import net.dv8tion.jda.api.events.Event
import kotlin.reflect.KParameter
import kotlin.reflect.full.callSuspendBy
import kotlin.reflect.full.instanceParameter
import kotlin.reflect.full.valueParameters

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

suspend fun insertAggregate(event: Event, aggregatedObjects: MutableMap<KParameter, Any?>, optionValues: MutableMap<Option, Any?>, parameter: CommandParameter) {
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