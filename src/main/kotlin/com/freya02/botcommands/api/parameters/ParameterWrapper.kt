package com.freya02.botcommands.api.parameters

import com.freya02.botcommands.api.core.utils.bestName
import com.freya02.botcommands.internal.utils.ReflectionUtils.function
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure
import com.freya02.botcommands.internal.utils.throwUser as utilsThrowUser

data class ParameterWrapper(
    val type: KType,
    val index: Int,
    val name: String,
    val parameter: KParameter? /* Nullable in case parameters would be "created" without a function */
) {
    constructor(parameter: KParameter) : this(parameter.type, parameter.index, parameter.bestName, parameter)

    val erasure = type.jvmErasure

    fun toListElementType() = when (type.jvmErasure) {
        List::class -> copy(
            type = type.arguments[0].type ?: throwUser("A concrete List element type is required")
        )
        else -> this
    }

    fun throwUser(message: String): Nothing = when (parameter) {
        null -> utilsThrowUser(message)
        else -> utilsThrowUser(parameter.function, message)
    }

    companion object {
        fun KParameter.wrap() = ParameterWrapper(this)
    }
}