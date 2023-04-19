package com.freya02.botcommands.api.parameters

import com.freya02.botcommands.internal.bestName
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.jvmErasure

data class ParameterWrapper(
    val type: KClass<*>,
    val index: Int,
    val name: String,
    val parameter: KParameter? /* Nullable in case parameters would be "created" without a function */
) {
    constructor(parameter: KParameter) : this(parameter.type.jvmErasure, parameter.index, parameter.bestName, parameter)

    fun throwUser(message: String) {
        if (parameter == null) {
            com.freya02.botcommands.internal.throwUser(message)
        } else {
            com.freya02.botcommands.internal.throwUser(parameter.function, message)
        }
    }

    companion object {
        fun KParameter.wrap() = ParameterWrapper(this)
    }
}