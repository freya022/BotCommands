package com.freya02.botcommands.internal

import com.freya02.botcommands.internal.utils.Utils
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.jvmErasure

inline fun <reified T : Enum<T>> enumSetOf(): EnumSet<T> = EnumSet.noneOf(T::class.java)
inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> = enumSetOf<T>().apply { addAll(elems) }

fun KClass<*>.isSubclassOfAny(vararg classes: KClass<*>): Boolean = classes.any { this.isSubclassOf(it) }

fun AbstractCommandInfo<*>.requireFirstParam(kParameters: List<KParameter>, klass: KClass<*>) = requireUser(klass.isSuperclassOf(kParameters[0].type.jvmErasure)) {
    "First argument should be a ${klass.simpleName}"
}

fun AbstractCommandInfo<*>.throwUser(message: String): Nothing = throwUser(commandMethod, message)

fun throwUser(function: KFunction<*>, message: String): Nothing =
    throw IllegalArgumentException("${Utils.formatMethodShort(function)} : $message")

@OptIn(ExperimentalContracts::class)
inline fun AbstractCommandInfo<*>.requireUser(value: Boolean, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    if (!value) {
        throwUser(lazyMessage())
    }
}