package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.internal.AbstractOption
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

interface AggregatedParameter {
    /**
     * **Note:** Can either be the user-defined aggregator or the command function
     *
     * See [AbstractOption.kParameter]
     */
    val typeCheckingFunction: KFunction<*>
    val typeCheckingParameterName: String
    val typeCheckingParameter: KParameter
}