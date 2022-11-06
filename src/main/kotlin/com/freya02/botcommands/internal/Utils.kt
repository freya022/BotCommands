package com.freya02.botcommands.internal

import com.freya02.botcommands.api.BCInfo
import com.freya02.botcommands.api.core.exceptions.InitializationException
import com.freya02.botcommands.api.core.exceptions.ServiceException
import com.freya02.botcommands.internal.utils.ReflectionUtilsKt.shortSignature
import net.dv8tion.jda.api.JDAInfo
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import java.lang.reflect.Modifier
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.*
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.jvmErasure

internal inline fun <reified T : Enum<T>> enumSetOf(): EnumSet<T> = EnumSet.noneOf(T::class.java)
internal inline fun <reified T : Enum<T>> enumSetOf(vararg elems: T): EnumSet<T> = enumSetOf<T>().apply { addAll(elems) }
internal inline fun <reified T : Enum<T>, V> enumMapOf(): EnumMap<T, V> = EnumMap<T, V>(T::class.java)

internal fun KClass<*>.isSubclassOfAny(vararg classes: KClass<*>): Boolean = classes.any { this.isSubclassOf(it) }
internal fun KClass<*>.isSubclassOfAny(classes: Iterable<KClass<*>>): Boolean = classes.any { this.isSubclassOf(it) }

internal fun IExecutableInteractionInfo.requireFirstParam(kParameters: List<KParameter>, klass: KClass<*>) {
    val firstParameter = kParameters.firstOrNull() ?: throwUser("First argument should be a ${klass.simpleName}")
    requireUser(klass.isSuperclassOf(firstParameter.type.jvmErasure)) {
        "First argument should be a ${klass.simpleName}"
    }
}

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
internal inline fun throwInternal(message: String): Nothing =
    throw IllegalArgumentException("$message, please report this to the devs. ${getDiagVersion()}")

internal fun getDiagVersion() = "[ BC version: ${BCInfo.VERSION} | Current JDA version: ${JDAInfo.VERSION} ]"

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
internal inline fun IExecutableInteractionInfo.throwUser(message: String): Nothing = throwUser(method, message)

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
internal inline fun throwUser(function: KFunction<*>, message: String): Nothing =
    throw IllegalArgumentException("${function.shortSignature} : $message")

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
internal inline fun rethrowUser(function: KFunction<*>, message: String, e: Throwable): Nothing =
    throw RuntimeException("${function.shortSignature} : $message", e)

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
internal inline fun throwUser(message: String): Nothing =
    throw IllegalArgumentException(message)

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
internal inline fun throwService(message: String, function: KFunction<*>? = null): Nothing = when (function) {
    null -> throw ServiceException(message)
    else -> throw ServiceException("${function.shortSignature} : $message")
}

@OptIn(ExperimentalContracts::class)
internal inline fun IExecutableInteractionInfo.requireUser(value: Boolean, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    requireUser(value, this.method, lazyMessage)
}

@OptIn(ExperimentalContracts::class)
internal inline fun requireUser(value: Boolean, function: KFunction<*>, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    if (!value) {
        throwUser(function, lazyMessage())
    }
}

val KParameter.isPrimitive: Boolean
    get() = this.type.jvmErasure.java.isPrimitive || this.type.jvmErasure.javaPrimitiveType != null

val KParameter.bestName: String
    get() = this.name ?: "arg${this.index}"

fun String.asDiscordString(): String {
    val sb: StringBuilder = StringBuilder()

    for (c in this) {
        if (c.isUpperCase()) {
            sb.append('_').append(c.lowercaseChar())
        } else {
            sb.append(c)
        }
    }

    return sb.toString()
}

fun KParameter.findDeclarationName(): String {
//    val annotatedName = findAnnotation<Name>()?.declaredName
//    if (!annotatedName.isNullOrBlank()) {
//        return annotatedName
//    }

    return name ?: throwUser("Parameter '$this' does not have any name information, please add the compiler options to include those (see wiki or readme)")
}

fun KParameter.findOptionName(): String {
//    val annotatedName = findAnnotation<Name>()?.name
//    if (!annotatedName.isNullOrBlank()) {
//        return annotatedName
//    }

    return name ?: throwUser("Parameter '$this' does not have any name information, please add the compiler options to include those (see wiki or readme)")
}

val KType.simpleName: String
    get() = (this.jvmErasure.simpleName ?: throwInternal("Tried to get the name of a no-name class: $this")) + if (this.isMarkedNullable) "?" else ""

fun KParameter.checkTypeEqualsIgnoreNull(param: KParameter): Boolean =
    this.type.jvmErasure == param.type.jvmErasure

val KFunction<*>.isPublic: Boolean
    get() = this.visibility == KVisibility.PUBLIC || this.visibility == KVisibility.INTERNAL

val KFunction<*>.isStatic: Boolean
    get() = Modifier.isStatic(this.javaMethod!!.modifiers)

inline fun <reified T : ReadWriteProperty<*, *>> KProperty0<*>.toDelegate(): T = this.also {it.isAccessible = true }.getDelegate() as T

inline fun <reified T> arrayOfSize(size: Int) = ArrayList<T>(size)

tailrec fun Throwable.getDeepestCause(): Throwable {
    if (cause == null) return this

    return cause!!.getDeepestCause()
}

inline fun <R> runInitialization(block: () -> R): R {
    try {
        return block()
    } catch (e: Throwable) {
        throw InitializationException("An exception occurred while building the framework", e)
    }
}

inline fun <T> Result<T>.onErrorResponseException(block: (ErrorResponseException) -> Unit): Result<T> {
    return also { onFailure { if (it is ErrorResponseException) block(it) } }
}

inline fun <T> Result<T>.onErrorResponse(block: (ErrorResponse) -> Unit): Result<T> {
    return onErrorResponseException { block(it.errorResponse) }
}

inline fun <T> Result<T>.onErrorResponse(error: ErrorResponse, block: (ErrorResponseException) -> Unit): Result<T> {
    return onErrorResponseException { if (it.errorResponse == error) block(it) }
}

internal inline fun <reified T> Any.throwMixin(): Nothing {
    throwInternal("${this::class.simpleName} should implement ${T::class.simpleName}")
}