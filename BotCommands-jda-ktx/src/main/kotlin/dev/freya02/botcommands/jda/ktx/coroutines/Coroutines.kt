package dev.freya02.botcommands.jda.ktx.coroutines

import dev.freya02.botcommands.jda.ktx.ReplaceJdaKtx
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.future.await
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.pagination.PaginationAction
import net.dv8tion.jda.api.utils.concurrent.Task
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Suspends until the REST request is finished and returns the result.
 */
@ReplaceJdaKtx("dev.minn.jda.ktx.coroutines")
suspend fun <T> RestAction<T>.await(): T = submit(true).await()

/**
 * Suspends until the gateway request is finished and returns the result.
 */
@ReplaceJdaKtx("dev.minn.jda.ktx.coroutines")
suspend fun <T> Task<T>.await(): T = suspendCancellableCoroutine { cont ->
    cont.invokeOnCancellation { cancel() }
    onSuccess { r -> cont.resume(r) }
    onError { e -> cont.resumeWithException(e) }
}

/**
 * Converts the pagination action to a [Flow].
 */
@ReplaceJdaKtx
fun <T> PaginationAction<T, *>.asFlow(): Flow<T> = flow {
    cache(false)
    var elements: List<T> = await()
    while (elements.isNotEmpty()) {
        elements.forEach { emit(it) }
        elements = await()
    }
}
