package com.freya02.botcommands.internal.parameters

import kotlin.reflect.KFunction

interface AggregatorParameter : AggregatedParameter {
    fun toOptionParameter(optionFunction: KFunction<*>, parameterName: String): OptionParameter

    companion object {
        fun fromUserAggregate(aggregator: KFunction<*>, parameterName: String): AggregatorParameter =
            UserAggregatorParameter(aggregator, parameterName)

        fun fromSelfAggregate(commandFunction: KFunction<*>, parameterName: String): AggregatorParameter =
            SingleAggregatorParameter(commandFunction, parameterName)

        fun fromVarargAggregate(commandFunction: KFunction<*>, parameterName: String): AggregatorParameter =
            VarargAggregatorParameter(commandFunction, parameterName)
    }
}