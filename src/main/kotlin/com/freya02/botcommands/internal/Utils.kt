package com.freya02.botcommands.internal

import com.freya02.botcommands.annotations.api.annotations.Optional
import com.freya02.botcommands.api.annotations.Name
import com.freya02.botcommands.internal.utils.Utils
import java.lang.reflect.Modifier
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure

inline fun <reified T : Enum<T>> enumSetOf(): EnumSet<T> = EnumSet.noneOf(T::class.java)
inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> = enumSetOf<T>().apply { addAll(elems) }

fun KClass<*>.isSubclassOfAny(vararg classes: KClass<*>): Boolean = classes.any { this.isSubclassOf(it) }

fun ExecutableInteractionInfo.requireFirstParam(kParameters: List<KParameter>, klass: KClass<*>) {
    val firstParameter = kParameters.firstOrNull() ?: throwUser("First argument should be a ${klass.simpleName}")
    requireUser(klass.isSuperclassOf(firstParameter.type.jvmErasure)) {
        "First argument should be a ${klass.simpleName}"
    }
}

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
fun throwInternal(message: String): Nothing = throw IllegalArgumentException("$message, please report this to the devs")

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
inline fun ExecutableInteractionInfo.throwUser(message: String): Nothing = throwUser(method, message)

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
inline fun throwUser(function: KFunction<*>, message: String): Nothing =
    throw IllegalArgumentException("${Utils.formatMethodShort(function)} : $message")

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
inline fun throwUser(message: String): Nothing =
    throw IllegalArgumentException(message)

@OptIn(ExperimentalContracts::class)
inline fun ExecutableInteractionInfo.requireUser(value: Boolean, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    requireUser(value, this.method, lazyMessage)
}

@OptIn(ExperimentalContracts::class)
inline fun requireUser(value: Boolean, function: KFunction<*>, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    if (!value) {
        throwUser(function, lazyMessage())
    }
}

val KParameter.isReallyOptional: Boolean
    get() {
        return isOptional || hasAnnotation<Optional>() //TODO take into account invisible annotations like Nullable, see ReflectionUtils
    }

val KParameter.isPrimitive: Boolean
    get() = this.type.jvmErasure.java.isPrimitive || this.type.jvmErasure.javaPrimitiveType != null

val KParameter.bestName: String
    get() = this.name ?: "<no-name>"

fun KParameter.findName(): String = when {
    name != null -> name!!
    else -> this.findAnnotation<Name>()?.name
        ?: throwUser("Parameter '$this' does not have any name information, please use the compiler options to include those (see wiki), or use @${Name::class.simpleName}")
}

val KType.simpleName: String
    get() = this.jvmErasure.simpleName ?: throwInternal("Tried to get the name of a no-name class: $this")

val KFunction<*>.isPublic: Boolean
    get() = this.visibility == KVisibility.PUBLIC

val KFunction<*>.isStatic: Boolean
    get() = Modifier.isStatic(this.javaMethod!!.modifiers)

inline fun <reified T> arrayOfSize(size: Int) = ArrayList<T>(size)