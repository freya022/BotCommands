package com.freya02.botcommands.internal

import com.freya02.botcommands.internal.application.CommandParameter
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.jvmErasure

class MethodParameters<T : CommandParameter<*>> private constructor(
    function: KFunction<*>,
    paramTransformer: (Int, KParameter) -> T
) : ArrayList<T>(function.parameters.size) {
    fun hasFirstParameter(klass: KClass<*>) = klass.isSuperclassOf(this[0].parameter.type.jvmErasure)

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