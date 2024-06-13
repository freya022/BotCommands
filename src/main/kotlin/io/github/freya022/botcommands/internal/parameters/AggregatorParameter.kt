package io.github.freya022.botcommands.internal.parameters

import kotlin.reflect.KFunction

internal interface AggregatorParameter : AggregatedParameter {
    fun toOptionParameter(optionFunction: KFunction<*>, parameterName: String): OptionParameter

    companion object {
        internal fun fromUserAggregate(aggregator: KFunction<*>, parameterName: String): AggregatorParameter =
            UserAggregatorParameter(aggregator, parameterName)

        internal fun fromSelfAggregate(commandFunction: KFunction<*>, parameterName: String): AggregatorParameter =
            SingleAggregatorParameter(commandFunction, parameterName)

        internal fun fromVarargAggregate(commandFunction: KFunction<*>, parameterName: String): AggregatorParameter =
            VarargAggregatorParameter(commandFunction, parameterName)
    }
}