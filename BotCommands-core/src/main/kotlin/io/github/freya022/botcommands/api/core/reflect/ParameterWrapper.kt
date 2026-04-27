package io.github.freya022.botcommands.api.core.reflect

import io.github.freya022.botcommands.api.core.utils.bestName
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.core.utils.getAllAnnotations
import io.github.freya022.botcommands.api.core.utils.hasAnnotationRecursive
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.function
import io.github.freya022.botcommands.internal.utils.isNullable
import io.github.freya022.botcommands.internal.utils.throwArgument as utilsThrowUser
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.KType

class ParameterWrapper private constructor(
    type: KType,
    val index: Int,
    val name: String,
    val parameter: KParameter
) {
    val typeToken: KotlinTypeToken<*> = KotlinTypeToken.ofType(type)
    val type: KType get() = typeToken.type
    val erasure: KClass<*> get() = typeToken.kotlinErasure
    val javaErasure: Class<*> get() = typeToken.javaErasure
    val annotations: List<Annotation> get() = parameter.getAllAnnotations()
    val isRequired get() = !parameter.isNullable && !parameter.isOptional

    constructor(parameter: KParameter) : this(parameter.type, parameter.index, parameter.bestName, parameter)

    /**
     * @see hasAnnotationRecursive
     */
    fun hasAnnotation(clazz: Class<out Annotation>): Boolean = parameter.hasAnnotationRecursive(clazz.kotlin)

    /**
     * @see findAnnotationRecursive
     */
    fun <A : Annotation> getAnnotation(clazz: Class<out A>): A? = parameter.findAnnotationRecursive(clazz.kotlin)

    @JvmSynthetic
    internal fun toListElementType() = when (erasure) {
        List::class -> ParameterWrapper(
            type = type.arguments[0].type ?: throwUser("A concrete List element type is required"),
            index = index,
            name = name,
            parameter = parameter
        )
        else -> this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParameterWrapper

        if (type != other.type) return false
        if (parameter != other.parameter) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + parameter.hashCode()
        return result
    }

    override fun toString(): String {
        return "ParameterWrapper(type=$type, parameter=$parameter)"
    }
}

internal val ParameterWrapper.function get() = parameter.function

/**
 * @see hasAnnotationRecursive
 */
inline fun <reified A : Annotation> ParameterWrapper.hasAnnotation(): Boolean = hasAnnotation(A::class.java)

/**
 * @see findAnnotationRecursive
 */
inline fun <reified A : Annotation> ParameterWrapper.findAnnotation(): A? = getAnnotation(A::class.java)

@OptIn(ExperimentalContracts::class)
@JvmSynthetic
internal inline fun ParameterWrapper.requireUser(value: Boolean, lazyMessage: () -> Any?) {
    contract {
        returns() implies value
        callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE)
    }

    if (!value) {
        utilsThrowUser(parameter.function, lazyMessage().toString())
    }
}

@JvmSynthetic
internal fun ParameterWrapper.throwUser(message: String): Nothing = utilsThrowUser(parameter.function, message)

// TODO shared internal
/** **INTERNAL** */
@JvmSynthetic
fun KParameter.wrap() = ParameterWrapper(this)
