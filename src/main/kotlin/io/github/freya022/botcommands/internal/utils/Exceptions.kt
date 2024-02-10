package io.github.freya022.botcommands.internal.utils

import io.github.freya022.botcommands.api.core.service.ServiceError
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

internal fun throwUser(function: KFunction<*>, message: String): Nothing =
    throw IllegalArgumentException("${function.shortSignature} : $message")

internal fun rethrowUser(function: KFunction<*>, message: String, e: Throwable): Nothing =
    throw RuntimeException("${function.shortSignature} : $message", e)

internal fun rethrowUser(message: String, e: Throwable): Nothing =
    throw RuntimeException(message, e)

internal fun throwUser(message: String): Nothing =
    throw IllegalArgumentException(message)

internal fun throwService(serviceError: ServiceError): Nothing =
    throw ServiceException("\n${serviceError.toDetailedString()}")

internal fun throwService(message: String, function: KFunction<*>? = null): Nothing = when (function) {
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

internal fun Throwable.unwrap(): Throwable {
    if (this is InvocationTargetException) return targetException
    return this
}