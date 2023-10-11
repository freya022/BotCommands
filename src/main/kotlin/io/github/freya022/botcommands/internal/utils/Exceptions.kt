package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.internal.core.exceptions.InternalException
import io.github.freya022.botcommands.internal.core.exceptions.ServiceException
import java.lang.reflect.InvocationTargetException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KFunction

internal fun throwInternal(message: String): Nothing =
    throw InternalException(message)

internal fun throwInternal(function: KFunction<*>, message: String): Nothing =
    throw InternalException("${function.shortSignature} : $message")

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
internal inline fun throwUser(function: KFunction<*>, message: String): Nothing =
    throw IllegalArgumentException("${function.shortSignature} : $message")

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
internal inline fun rethrowUser(function: KFunction<*>, message: String, e: Throwable): Nothing =
    throw RuntimeException("${function.shortSignature} : $message", e)

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
internal inline fun rethrowUser(message: String, e: Throwable): Nothing =
    throw RuntimeException(message, e)

@PublishedApi
@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
internal inline fun throwUser(message: String): Nothing =
    throw IllegalArgumentException(message)

@Suppress("NOTHING_TO_INLINE") //Don't want this to appear in stack trace
internal inline fun throwService(message: String, function: KFunction<*>? = null): Nothing = when (function) {
    null -> throw ServiceException(message)
    else -> throw ServiceException("${function.shortSignature} : $message")
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

@OptIn(ExperimentalContracts::class)
internal inline fun requireUser(value: Boolean, lazyMessage: () -> String) {
    contract {
        returns() implies value
    }

    if (!value) {
        throwUser(lazyMessage())
    }
}

internal inline fun <reified T> Any.throwMixin(): Nothing {
    throwInternal("${this::class.simpleName} should implement ${T::class.simpleName}")
}

internal fun Throwable.unwrap(): Throwable {
    if (this is InvocationTargetException) return targetException
    return this
}