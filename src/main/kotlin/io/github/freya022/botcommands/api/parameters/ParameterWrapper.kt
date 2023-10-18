package io.github.freya022.botcommands.api.parameters

import io.github.freya022.botcommands.api.core.utils.bestName
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import kotlin.reflect.KParameter
import kotlin.reflect.KType
import kotlin.reflect.jvm.jvmErasure
import io.github.freya022.botcommands.internal.utils.throwUser as utilsThrowUser

data class ParameterWrapper internal constructor(
    val type: KType,
    val index: Int,
    val name: String,
    val parameter: KParameter? /* Nullable in case parameters would be "created" without a function */
) {
    val erasure = type.jvmErasure

    internal constructor(parameter: KParameter) : this(parameter.type, parameter.index, parameter.bestName, parameter)

    @JvmSynthetic
    internal fun toListElementType() = when (type.jvmErasure) {
        List::class -> copy(
            type = type.arguments[0].type ?: throwUser("A concrete List element type is required")
        )
        else -> this
    }

    @JvmSynthetic
    internal fun throwUser(message: String): Nothing = when (parameter) {
        null -> utilsThrowUser(message)
        else -> utilsThrowUser(parameter.function, message)
    }
}

fun KParameter.wrap() = ParameterWrapper(this)