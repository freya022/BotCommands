@file:OptIn(ExperimentalContracts::class)

package io.github.freya022.botcommands.api.core.utils

import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Encapsulates a successful outcome or a failure.
 *
 * Failures can be dismissed as long as the value isn't retrieved.
 *
 * @see runCatchingResponse
 * @see runIgnoringResponse
 * @see runIgnoringResponseOrNull
 */
class RestResult<out T>(val result: Result<T>) {
    /**
     * Returns `true` if this instance represents a successful outcome.
     * In this case [isFailure] returns `false`.
     */
    val isSuccess: Boolean
        get() = result.isSuccess

    /**
     * Returns `true` if this instance represents a failed outcome.
     * In this case [isSuccess] returns `false`.
     */
    val isFailure: Boolean
        get() = result.isFailure

    /**
     * Whether this result's exception has been ignored.
     *
     * Methods attempting to read a value will still throw the ignored exception.
     */
    var isIgnoringExceptions: Boolean = true
        private set

    /**
     * Returns the encapsulated value if this instance represents [success][RestResult.isSuccess]
     * or throws the encapsulated [Throwable] exception if it is [failure][RestResult.isFailure].
     */
    fun getOrThrow(): T = result.getOrThrow()

    /**
     * Returns the encapsulated value if this instance represents [success][RestResult.isSuccess]
     * or `null` if it is [failure][RestResult.isFailure].
     */
    fun getOrNull(): T? = result.getOrNull()

    /**
     * Throws the encapsulated [Throwable] exception if it is [failure][RestResult.isFailure]
     * and it is not [ignored][isIgnoringExceptions].
     */
    fun orThrow() {
        throw result.exceptionOrNull() ?: return
    }

    /**
     * Returns the encapsulated [Throwable] exception if this instance represents [failure][isFailure] or `null`
     * if it is [success][isSuccess].
     *
     * Ignored exceptions are still returned, use [exceptionOrNullIfIgnored] instead
     */
    fun exceptionOrNull(): Throwable? =
        result.exceptionOrNull()

    /**
     * Returns the encapsulated [Throwable] exception if this instance represents [failure][isFailure] or `null`
     * if it is [success][isSuccess] or the exception is [ignored][isIgnoringExceptions].
     */
    fun exceptionOrNullIfIgnored(): Throwable? =
        result.exceptionOrNull()?.takeUnless { isIgnoringExceptions }

    /**
     * Runs the given [block] on the encapsulated value if this instance represents [success][RestResult.isSuccess].
     *
     * Returns the original `RestResult` unchanged.
     */
    inline fun onSuccess(block: (T) -> Unit): RestResult<T> {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }

        result.onSuccess(block)
        return this
    }

    /**
     * Runs the given [block] on the encapsulated [Throwable] exception
     * if this instance represents [failure][RestResult.isFailure],
     * and the exception is not [ignored][isIgnoringExceptions].
     *
     * Returns the original `RestResult` unchanged.
     */
    inline fun onFailure(block: (Throwable) -> Unit): RestResult<T> {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }

        if (result.isSuccess) return this
        if (isIgnoringExceptions) return this

        result.onFailure(block)
        return this
    }

    /**
     * Dismisses the encapsulated [error response][ErrorResponse]
     * if it corresponds to an ignored response.
     * 
     * Allows for [orThrow] to be used on failures without throwing,
     * but does not allow using functions returning values.
     *
     * Returns the original `RestResult` unchanged.
     *
     * @see handle
     */
    fun ignore(vararg responses: ErrorResponse) = apply {
        val it = result.exceptionOrNull() ?: return@apply
        isIgnoringExceptions = isIgnoringExceptions && (it is ErrorResponseException && it.errorResponse in responses)
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
 * @see RestResult.ignore
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
 * @see RestResult.ignore
 * @see handle
 */
inline fun <T> RestResult<T>.onErrorResponse(error: ErrorResponse, block: (ErrorResponseException) -> Unit): RestResult<T> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    return onErrorResponseException { if (it.errorResponse == error) block(it) }
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
 * @see RestResult.ignore
 */
inline fun <T : R, R> RestResult<T>.recover(vararg responses: ErrorResponse, block: (ErrorResponseException) -> R): RestResult<R> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    val it = result.exceptionOrNull()
    return if (it is ErrorResponseException && it.errorResponse in responses) {
        runCatchingRest { block(it) }
    } else {
        this
    }
}

/**
 * Dismisses the encapsulated [error response][ErrorResponse] and runs the given [block]
 * if it corresponds to an ignored response.
 *
 * Any thrown exception will be encapsulated in a new [RestResult].
 *
 * Returns the original `RestResult` unchanged otherwise.
 *
 * @see RestResult.ignore
 */
inline fun <T> RestResult<T>.handle(vararg responses: ErrorResponse, block: (ErrorResponseException) -> Unit): RestResult<T> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    val it = result.exceptionOrNull() ?: return this
    if (it is ErrorResponseException && it.errorResponse in responses) {
        try {
            block(it)
            return this
        } catch (e: Throwable) {
            return RestResult(Result.failure(e))
        } finally { // For non-local returns
            ignore(*responses)
        }
    } else {
        return this
    }
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

    return RestResult(runCatching(block))
}