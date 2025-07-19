package dev.freya02.botcommands.jda.ktx.requests

import dev.freya02.botcommands.jda.ktx.DeprecatedInBcCore
import dev.freya02.botcommands.jda.ktx.coroutines.await
import net.dv8tion.jda.api.exceptions.ErrorHandler
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.RestAction

/**
 * Awaits the completion of this RestAction.
 */
@DeprecatedInBcCore
suspend fun RestAction<*>.awaitUnit() {
    await()
}

/**
 * Awaits the completion of this RestAction and returns `null`.
 */
@DeprecatedInBcCore
suspend fun <R> RestAction<*>.awaitNull(): R? {
    await()
    return null
}

/**
 * Queues this REST action while ignoring the provided error responses.
 *
 * Any other error will be handled using the [default failure consumer][RestAction.setDefaultFailure].
 *
 * @see ErrorHandler
 */
@DeprecatedInBcCore
fun RestAction<*>.queueIgnoring(vararg errorResponses: ErrorResponse) {
    queue(null, ErrorHandler().ignore(errorResponses.asList()))
}

/**
 * Awaits the completion of this RestAction,
 * returns `null` if one of the provided error responses was thrown.
 *
 * Any other exception or error response occurs will be thrown.
 *
 * @see runIgnoringResponseOrNull
 */
@DeprecatedInBcCore
suspend fun <R> RestAction<R>.awaitOrNullOn(vararg errorResponses: ErrorResponse): R? {
    return runIgnoringResponseOrNull(*errorResponses) {
        await()
    }
}

/**
 * Awaits the completion of this RestAction and wraps it in a Result.
 */
@DeprecatedInBcCore
suspend fun <R> RestAction<R>.awaitCatching(): RestResult<R> {
    return runCatchingRest { await() }
}
