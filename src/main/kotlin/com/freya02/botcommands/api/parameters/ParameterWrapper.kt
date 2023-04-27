package com.freya02.botcommands.api.parameters

import com.freya02.botcommands.internal.bestName
import com.freya02.botcommands.internal.utils.ReflectionMetadata.function
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.jvm.jvmErasure

data class ParameterWrapper(
    val type: KType,
    val index: Int,
    val name: String,
    val parameter: KParameter? /* Nullable in case parameters would be "created" without a function */
) {
    constructor(parameter: KParameter) : this(parameter.type, parameter.index, parameter.bestName, parameter)

    val erasure = type.jvmErasure

    fun toVarargElementType() = when {
        parameter!!.isVararg -> copy(
            //kotlin moment
            type = type.jvmErasure.java.componentType.kotlin
                .createType(type.arguments, type.isMarkedNullable, type.annotations)
        )

        else -> this
    }

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