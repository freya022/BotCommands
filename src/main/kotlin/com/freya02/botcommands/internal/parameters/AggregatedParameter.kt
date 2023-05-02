package com.freya02.botcommands.internal.parameters

import com.freya02.botcommands.internal.core.options.Option
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter

interface AggregatedParameter {
    /**
     * **Note:** Can either be the user-defined aggregator or the command function
     *
     * See [Option.kParameter]
     */
    val typeCheckingFunction: KFunction<*>
    val typeCheckingParameterName: String
    val typeCheckingParameter: KParameter
}