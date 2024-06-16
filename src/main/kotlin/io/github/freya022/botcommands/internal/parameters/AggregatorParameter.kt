package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.api.core.options.Option
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

internal interface AggregatorParameter {
    /**
     * **Note:** Can either be the user-defined aggregator or the command function
     *
     * See [Option.kParameter]
     */
    val typeCheckingFunction: KFunction<*>
    val typeCheckingParameterName: String
    val typeCheckingParameter: KParameter

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