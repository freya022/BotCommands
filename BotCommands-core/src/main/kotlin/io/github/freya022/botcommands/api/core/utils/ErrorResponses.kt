@file:OptIn(ExperimentalContracts::class)

package io.github.freya022.botcommands.api.core.utils

import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.requests.ErrorResponse
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Encapsulates the result of the specified function [block] and dismisses [error responses][ErrorResponse]
 * that corresponds to an ignored response, making the [Result] a success.
 *
 * @see runIgnoringResponse
 * @see runIgnoringResponseOrNull
 */
inline fun runCatchingResponse(vararg ignoredResponses: ErrorResponse, block: () -> Unit): RestResult<Unit> {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return runCatchingRest(block).ignore(*ignoredResponses)
}

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
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

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
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    return try {
        block()
    } catch (e: ErrorResponseException) {
        if (e.errorResponse !in ignoredResponses) {
            throw e
        }
        null
    }
}