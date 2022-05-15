package com.freya02.botcommands.internal

import com.freya02.botcommands.annotations.api.annotations.Optional
import com.freya02.botcommands.internal.utils.Utils
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.hasAnnotation
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure

inline fun <reified T : Enum<T>> enumSetOf(): EnumSet<T> = EnumSet.noneOf(T::class.java)
inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> = enumSetOf<T>().apply { addAll(elems) }

fun KClass<*>.isSubclassOfAny(vararg classes: KClass<*>): Boolean = classes.any { this.isSubclassOf(it) }

fun ExecutableInteractionInfo.requireFirstParam(kParameters: List<KParameter>, klass: KClass<*>) = requireUser(klass.isSuperclassOf(kParameters[0].type.jvmErasure)) {
    "First argument should be a ${klass.simpleName}"
}

fun ExecutableInteractionInfo.throwUser(message: String): Nothing = throwUser(method, message)

fun throwUser(function: KFunction<*>, message: String): Nothing =
    throw IllegalArgumentException("${Utils.formatMethodShort(function)} : $message")

@OptIn(ExperimentalContracts::class)
inline fun ExecutableInteractionInfo.requireUser(value: Boolean, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    if (!value) {
        throwUser(lazyMessage())
    }
}

val KParameter.isReallyOptional: Boolean
    get() {
        return isOptional || hasAnnotation<Optional>() //TODO take into account invisible annotations like Nullable, see ReflectionUtils
    }

val KParameter.isPrimitive: Boolean
    get() = this.type.jvmErasure.java.isPrimitive || this.type.jvmErasure.javaPrimitiveType != null

fun throwInternal(message: String): Nothing = throw IllegalArgumentException("$message, please report this to the devs")

inline fun <reified T> arrayOfSize(size: Int) = ArrayList<T>(size)