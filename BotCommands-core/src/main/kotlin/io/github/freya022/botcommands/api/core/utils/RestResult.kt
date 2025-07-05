@file:OptIn(ExperimentalContracts::class)
@file:Suppress("UNCHECKED_CAST")

package io.github.freya022.botcommands.api.core.utils

import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

/**
 * Encapsulates a successful outcome or a failure.
 *
 * Failures can be dismissed as long as the value isn't retrieved.
 *
 * @see runCatchingResponse
 * @see runIgnoringResponse
 * @see runIgnoringResponseOrNull
 */
@JvmInline
value class RestResult<out T> @PublishedApi internal constructor(
    @PublishedApi internal val value: Any?
) {
    @PublishedApi
    internal sealed class Failure(val exception: Throwable) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Failure

            return exception == other.exception
        }

        override fun hashCode(): Int = exception.hashCode()
    }

    internal class FatalFailure(exception: Throwable) : Failure(exception) {
        override fun toString(): String = "FatalFailure($exception)"
    }
    @PublishedApi
    internal class IgnoredFailure(exception: Throwable) : Failure(exception) {
        override fun toString(): String = "IgnoredFailure($exception)"
    }

    /**
     * Returns `true` if this instance represents a successful outcome.
     * In this case [isFailure] returns `false`.
     */
    val isSuccess: Boolean
        get() = value !is Failure

    /**
     * Returns `true` if this instance represents a failed outcome.
     * In this case [isSuccess] returns `false`.
     */
    val isFailure: Boolean
        get() = value is Failure

    /**
     * Whether this result's exception has been ignored.
     *
     * Methods attempting to read a value will still throw the ignored exception.
     */
    val isIgnoredException: Boolean
        get() = value is IgnoredFailure

    /**
     * Returns the encapsulated value if this instance represents [success][isSuccess]
     * or throws the encapsulated [Throwable] exception if it is [failure][isFailure].
     */
    fun getOrThrow(): T = when {
        value is Failure -> throw value.exception
        else -> value as T
    }

    /**
     * Returns the encapsulated value if this instance represents [success][isSuccess]
     * or `null` if it is [failure][isFailure].
     */
    fun getOrNull(): T? = when {
        value is Failure -> null
        else -> value as T
    }

    /**
     * Throws the encapsulated [Throwable] exception if it is [failure][isFailure]
     * and it is not [ignored][isIgnoredException].
     */
    fun orThrow() {
        // Only throw if not ignored
        if (value is FatalFailure)
            throw value.exception
    }

    /**
     * Returns the encapsulated [Throwable] exception if this instance represents [failure][isFailure] or `null`
     * if it is [success][isSuccess].
     *
     * Ignored exceptions are still returned, use [exceptionOrNullIfIgnored] instead
     */
    fun exceptionOrNull(): Throwable? =
        when (value) {
            is Failure -> value.exception
            else -> null
        }

    /**
     * Returns the encapsulated [Throwable] exception if this instance represents [failure][isFailure] or `null`
     * if it is [success][isSuccess] or the exception is [ignored][isIgnoredException].
     */
    fun exceptionOrNullIfIgnored(): Throwable? =
        exceptionOrNull()?.takeUnless { isIgnoredException }

    /**
     * Runs the given [block] on the encapsulated value if this instance represents [success][isSuccess].
     *
     * Returns the original `RestResult` unchanged.
     *
     * Thrown exceptions inside the block are rethrown.
     */
    inline fun onSuccess(block: (T) -> Unit): RestResult<T> {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }

        if (isSuccess)
            block(value as T)
        return this
    }

    /**
     * Runs the given [block] on the encapsulated [Throwable] exception
     * if this instance represents [failure][isFailure],
     * and the exception is not [ignored][isIgnoredException].
     *
     * Returns the original `RestResult` unchanged.
     */
    inline fun onFailure(block: (Throwable) -> Unit): RestResult<T> {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }

        exceptionOrNullIfIgnored()?.let(block)
        return this
    }

    override fun toString(): String = when (value) {
        is Failure -> value.toString()
        else -> "Success($value)"
    }

    companion object {
        /**
         * Returns an instance that encapsulates the given [value] as successful value.
         */
        fun <T> success(value: T): RestResult<T> = RestResult(value)

        /**
         * Returns an instance that encapsulates the given [Throwable] [exception] as failure.
         */
        fun <T> failure(exception: Throwable): RestResult<T> = RestResult(FatalFailure(exception))
    }
}

/**
 * Runs the given [block] if the result is an [ErrorResponseException].
 *
 * This does not clear the exception.
 *
 * Returns the original `RestResult` unchanged.
 */
inline fun <T> RestResult<T>.onErrorResponseException(block: (ErrorResponseException) -> Unit): RestResult<T> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return onFailure { if (it is ErrorResponseException) block(it) }
}

/**
 * Runs the given [block] if the result is an [error response][ErrorResponse].
 *
 * This does not clear the exception.
 *
 * Returns the original `RestResult` unchanged.
 *
 * @see ignore
 * @see handle
 */
inline fun <T> RestResult<T>.onErrorResponse(block: (ErrorResponse) -> Unit): RestResult<T> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return onErrorResponseException { block(it.errorResponse) }
}

/**
 * Runs the given [block] if the result is the specified [error response][ErrorResponse].
 *
 * This does not clear the exception.
 *
 * Returns the original `RestResult` unchanged.
 *
 * @see ignore
 * @see handle
 */
inline fun <T> RestResult<T>.onErrorResponse(error: ErrorResponse, block: (ErrorResponseException) -> Unit): RestResult<T> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return onErrorResponseException { if (it.errorResponse == error) block(it) }
}

/**
 * Dismisses the encapsulated exception if it corresponds to an predicate.
 *
 * Allows for [orThrow][RestResult.orThrow] to be used on failures without throwing,
 * but does not allow using functions returning values.
 *
 * Returns a new `RestResult` if the exception matches.
 *
 * @see handle
 */
inline fun <T> RestResult<T>.ignore(predicate: (Throwable) -> Boolean): RestResult<T> {
    contract {
        callsInPlace(predicate, InvocationKind.AT_MOST_ONCE)
    }

    if (value !is RestResult.FatalFailure) return this

    val it = value.exception
    return if (predicate(it)) {
        RestResult(RestResult.IgnoredFailure(it))
    } else {
        this
    }
}

/**
 * Dismisses the encapsulated [error response][ErrorResponse]
 * if it corresponds to an ignored response.
 *
 * Allows for [orThrow][RestResult.orThrow] to be used on failures without throwing,
 * but does not allow using functions returning values.
 *
 * Returns a new `RestResult` if the exception matches.
 *
 * @see handle
 */
fun <T> RestResult<T>.ignore(vararg responses: ErrorResponse): RestResult<T> =
    ignore { it is ErrorResponseException && it.errorResponse in responses }

/**
 * Dismisses the encapsulated exception
 * if it corresponds to an ignored exception.
 *
 * Allows for [orThrow][RestResult.orThrow] to be used on failures without throwing,
 * but does not allow using functions returning values.
 *
 * Returns a new `RestResult` if the exception matches.
 *
 * @see handle
 */
fun <T> RestResult<T>.ignore(vararg types: KClass<out Throwable>): RestResult<T> =
    ignore { throwable -> types.any { it.isInstance(throwable) } }

inline fun <T : R, R> RestResult<T>.recover(predicate: (Throwable) -> Boolean, block: (Throwable) -> R): RestResult<R> {
    contract {
        callsInPlace(predicate, InvocationKind.AT_MOST_ONCE)
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    val it = exceptionOrNull()
    return if (it != null && predicate(it)) {
        runCatchingRest { block(it) }
    } else {
        this
    }
}

/**
 * Maps the encapsulated [error response][ErrorResponse] using the given function [block]
 * if it corresponds to an ignored response.
 *
 * Exceptions other than [responses] will be rethrown in a new [RestResult].
 *
 * Any thrown exception will be encapsulated in a new [RestResult].
 *
 * May return a new `RestResult`.
 *
 * @see ignore
 */
inline fun <T : R, R> RestResult<T>.recover(vararg responses: ErrorResponse, block: (ErrorResponseException) -> R): RestResult<R> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return recover(
        predicate = { it is ErrorResponseException && it.errorResponse in responses },
        block = { block(it as ErrorResponseException) }
    )
}

inline fun <T : R, R> RestResult<T>.recover(vararg types: KClass<out Throwable>, block: (Throwable) -> R): RestResult<R> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return recover(
        predicate = { exception -> types.any { it.isInstance(exception) } },
        block = block
    )
}

inline fun <T> RestResult<T>.handle(predicate: (Throwable) -> Boolean, block: (Throwable) -> Unit): RestResult<T> {
    contract {
        callsInPlace(predicate, InvocationKind.UNKNOWN)
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    val it = exceptionOrNull()
    if (it != null && predicate(it)) {
        try {
            block(it)
            return ignore(predicate)
        } catch (e: Throwable) {
            return RestResult.failure(e)
        }
    } else {
        return this
    }
}

/**
 * Dismisses the encapsulated [error response][ErrorResponse] and runs the given [block]
 * if it corresponds to an ignored response.
 *
 * Any thrown exception will be encapsulated in a new [RestResult].
 *
 * Returns a new `RestResult` with the ignored exception, or itself if it didn't match.
 *
 * @see ignore
 */
inline fun <T> RestResult<T>.handle(vararg responses: ErrorResponse, block: (ErrorResponseException) -> Unit): RestResult<T> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return handle(
        predicate = { it is ErrorResponseException && it.errorResponse in responses },
        block = { block(it as ErrorResponseException) }
    )
}

inline fun <T> RestResult<T>.handle(vararg types: KClass<out Throwable>, block: (Throwable) -> Unit): RestResult<T> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return handle(
        predicate = { exception -> types.any { it.isInstance(exception) } },
        block = block
    )
}

/**
 * Calls the specified function [block] and returns its encapsulated result if invocation was successful,
 * catching any [Throwable] exception that was thrown from the [block] function execution
 * and encapsulating it as a failure.
 */
inline fun <T> runCatchingRest(block: () -> T): RestResult<T> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return try {
        RestResult.success(block())
    } catch (e: Throwable) {
        RestResult.failure(e)
    }
}