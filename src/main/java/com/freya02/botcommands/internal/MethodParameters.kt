package com.freya02.botcommands.internal

import com.freya02.botcommands.internal.application.CommandParameter
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.valueParameters

class MethodParameters<T : CommandParameter<*>> private constructor(
    function: KFunction<*>,
    paramTransformer: (Int, KParameter) -> T
) : ArrayList<T>(function.parameters.size) {
    val optionCount: Int

    //TODO Maybe it would be nice to do different object types for custom resolver, and proper types for command parameters, instead of smashing both together.
    // This currently becomes a problem when making options from DSL builders as you can't easily keep track of option indexes, or includes unnecessary/unwanted processing
    // Make a common interface for name / type though

    init {
        addAll(
            function.valueParameters.drop(1).mapIndexed(paramTransformer)
        )

        optionCount = this.count { it.isOption }
    }

    companion object {
        @JvmStatic //TODO remove once internal is in kotlin
        fun <T : CommandParameter<*>> of(
            method: KFunction<*>,
            paramTransformer: (Int, KParameter) -> T
        ): MethodParameters<T> {
            return MethodParameters(method, paramTransformer)
        }
    }
}