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

    init {
        addAll(
            function.valueParameters.mapIndexed(paramTransformer)
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