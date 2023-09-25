package com.freya02.botcommands.api.core.utils

import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse

//TODO Usages of this function are kinda sus in UserResolver/MemberResolver
//TODO docs
// This does not dismiss the exception
inline fun <T> Result<T>.onErrorResponseException(block: (ErrorResponseException) -> Unit): Result<T> {
    return onFailure { if (it is ErrorResponseException) block(it) }
}

//TODO docs
// This does not dismiss the exception
inline fun <T> Result<T>.onErrorResponse(block: (ErrorResponse) -> Unit): Result<T> {
    return onErrorResponseException { block(it.errorResponse) }
}

//TODO docs
// This does not dismiss the exception
inline fun <T> Result<T>.onErrorResponse(error: ErrorResponse, block: (ErrorResponseException) -> Unit): Result<T> {
    return onErrorResponseException { if (it.errorResponse == error) block(it) }
}

//TODO docs
fun Result<Unit>.ignore(vararg responses: ErrorResponse): Result<Unit> = recoverCatching {
    if (it is ErrorResponseException && it.errorResponse in responses) {
        // Ignore
    } else {
        throw it
    }
}

//TODO docs
// This *does* dismiss the exception
inline fun <T : R, R> Result<T>.handle(vararg responses: ErrorResponse, block: (ErrorResponseException) -> R): Result<R> = recoverCatching {
    if (it is ErrorResponseException && it.errorResponse in responses) {
        block(it)
    } else {
        throw it
    }
}

//TODO docs
inline fun runCatchingResponse(vararg ignoredResponses: ErrorResponse, block: () -> Unit): Result<Unit> =
    runCatching(block).ignore(*ignoredResponses)

//TODO docs
inline fun runIgnoringResponse(vararg ignoredResponses: ErrorResponse, block: () -> Unit) {
    try {
        block()
    } catch (e: ErrorResponseException) {
        if (e.errorResponse !in ignoredResponses) {
            throw e
        }
    }
}

//TODO docs
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