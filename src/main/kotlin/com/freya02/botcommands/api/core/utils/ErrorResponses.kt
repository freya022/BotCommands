package com.freya02.botcommands.api.core.utils

import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse

inline fun <T> Result<T>.onErrorResponseException(block: (ErrorResponseException) -> Unit): Result<T> {
    return also { onFailure { if (it is ErrorResponseException) block(it) } }
}

inline fun <T> Result<T>.onErrorResponse(block: (ErrorResponse) -> Unit): Result<T> {
    return onErrorResponseException { block(it.errorResponse) }
}

inline fun <T> Result<T>.onErrorResponse(error: ErrorResponse, block: (ErrorResponseException) -> Unit): Result<T> {
    return onErrorResponseException { if (it.errorResponse == error) block(it) }
}