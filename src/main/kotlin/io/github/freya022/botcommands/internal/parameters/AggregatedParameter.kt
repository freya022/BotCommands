package io.github.freya022.botcommands.internal.parameters

import io.github.freya022.botcommands.internal.core.options.Option
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