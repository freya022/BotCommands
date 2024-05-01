package io.github.freya022.botcommands.api.core.utils

import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse

/**
 * Runs the function [block] if the result is an [ErrorResponseException].
 *
 * This does not clear the exception.
 */
inline fun <T> Result<T>.onErrorResponseException(block: (ErrorResponseException) -> Unit): Result<T> {
    return onFailure { if (it is ErrorResponseException) block(it) }
}

/**
 * Runs the function [block] if the result is an [error response][ErrorResponse].
 *
 * This does not clear the exception.
 *
 * @see ignore
 * @see handle
 */
inline fun <T> Result<T>.onErrorResponse(block: (ErrorResponse) -> Unit): Result<T> {
    return onErrorResponseException { block(it.errorResponse) }
}

/**
 * Runs the function [block] if the result is the specified [error response][ErrorResponse].
 *
 * This does not clear the exception.
 *
 * @see ignore
 * @see handle
 */
inline fun <T> Result<T>.onErrorResponse(error: ErrorResponse, block: (ErrorResponseException) -> Unit): Result<T> {
    return onErrorResponseException { if (it.errorResponse == error) block(it) }
}

/**
 * Dismisses the encapsulated [error response][ErrorResponse]
 * if it corresponds to an ignored response, making the [Result] a success.
 *
 * @see handle
 * @see runCatchingResponse
 * @see runIgnoringResponse
 * @see runIgnoringResponseOrNull
 */
fun Result<Unit>.ignore(vararg responses: ErrorResponse): Result<Unit> = recoverCatching {
    if (it is ErrorResponseException && it.errorResponse in responses) {
        // Ignore
    } else {
        throw it
    }
}

/**
 * Maps the encapsulated [error response][ErrorResponse] using the given function [block]
 * if it corresponds to an ignored response.
 *
 * Any thrown exception will be encapsulated in the returned [Result].
 *
 * @see ignore
 */
inline fun <T : R, R> Result<T>.handle(vararg responses: ErrorResponse, block: (ErrorResponseException) -> R): Result<R> = recoverCatching {
    if (it is ErrorResponseException && it.errorResponse in responses) {
        block(it)
    } else {
        throw it
    }
}

/**
 * Encapsulates the result of the specified function [block] and dismisses [error responses][ErrorResponse]
 * that corresponds to an ignored response, making the [Result] a success.
 *
 * @see runIgnoringResponse
 * @see runIgnoringResponseOrNull
 */
inline fun runCatchingResponse(vararg ignoredResponses: ErrorResponse, block: () -> Unit): Result<Unit> =
    runCatching(block).ignore(*ignoredResponses)

/**
 * Runs the specified function [block] and dismisses [error responses][ErrorResponse]
 * that corresponds to an ignored response.
 *
 * Any other exception is still thrown.
 *
 * @see ignore
 * @see runIgnoringResponseOrNull
 */
inline fun runIgnoringResponse(vararg ignoredResponses: ErrorResponse, block: () -> Unit) {
    try {
        block()
    } catch (e: ErrorResponseException) {
        if (e.errorResponse !in ignoredResponses) {
            throw e
        }
    }
}

/**
 * Runs the specified function [block] and returns `null` on [error responses][ErrorResponse]
 * that corresponds to an ignored response.
 *
 * Any other exception is still thrown.
 *
 * @see ignore
 * @see runIgnoringResponse
 * @see awaitOrNullOn
 */
inline fun <R> runIgnoringResponseOrNull(vararg ignoredResponses: ErrorResponse, block: () -> R): R? {
    return try {
        block()
    } catch (e: ErrorResponseException) {
        if (e.errorResponse !in ignoredResponses) {
            throw e
        }
        null
    }
}