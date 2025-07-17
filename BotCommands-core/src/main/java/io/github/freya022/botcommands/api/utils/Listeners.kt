package io.github.freya022.botcommands.api.utils

import io.github.freya022.botcommands.api.core.hooks.CoroutineEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.sharding.ShardManager
import kotlin.coroutines.resume
import kotlin.time.Duration

/**
 * Creates and registers an event listener fired for every event of type [T] on this shard.
 *
 * If [timeout] is `null`, non-positive or non-finite,
 * the [default timeout][io.github.freya022.botcommands.api.core.config.BEventManagerConfig.defaultTimeout] will be used.
 *
 * @param timeout The duration for which this listener's code is allowed to run for.
 * @param block   The event consumer
 */
inline fun <reified T : GenericEvent> JDA.listener(
    timeout: Duration? = null,
    crossinline block: suspend CoroutineEventListener.(event: T) -> Unit,
) {
    eventManager.register(object : CoroutineEventListener {
        override val timeout: Duration? = timeout

        override fun cancel() {
            eventManager.unregister(this)
        }

        override suspend fun onEvent(event: GenericEvent) {
            if (event is T)
                block(event)
        }
    })
}

/**
 * Creates and registers an event listener fired for every event of type [T] on every shard.
 *
 * If [timeout] is `null`, non-positive or non-finite,
 * the [default timeout][io.github.freya022.botcommands.api.core.config.BEventManagerConfig.defaultTimeout] will be used.
 *
 * @param timeout The duration for which this listener's code is allowed to run for.
 * @param block   The event consumer
 */
inline fun <reified T : GenericEvent> ShardManager.listener(
    timeout: Duration? = null,
    crossinline block: suspend CoroutineEventListener.(event: T) -> Unit,
) {
    addEventListener(object : CoroutineEventListener {
        override val timeout: Duration? = timeout

        override fun cancel() {
            removeEventListener(this)
        }

        override suspend fun onEvent(event: GenericEvent) {
            if (event is T)
                block(event)
        }
    })
}

/**
 * Suspends until an event of type [T] satisfying the [filter] is received on this shard, then returns it.
 *
 * If you wish to use a timeout with it, you can use [withTimeoutOrNull][kotlinx.coroutines.withTimeoutOrNull].
 */
suspend inline fun <reified T : GenericEvent> JDA.await(
    crossinline filter: (T) -> Boolean = { true },
): T = suspendCancellableCoroutine { cont ->
    val listener = object : EventListener {
        override fun onEvent(event: GenericEvent) {
            if (event is T && filter(event)) {
                removeEventListener(this)
                cont.resume(event)
            }
        }
    }
    addEventListener(listener)
    cont.invokeOnCancellation { removeEventListener(listener) }
}

/**
 * Suspends until an event of type [T] satisfying the [filter] is received on any shard, then returns it.
 *
 * If you wish to use a timeout with it, you can use [withTimeoutOrNull][kotlinx.coroutines.withTimeoutOrNull].
 */
suspend inline fun <reified T : GenericEvent> ShardManager.await(
    crossinline filter: (T) -> Boolean = { true },
): T = suspendCancellableCoroutine { cont ->
    val listener = object : EventListener {
        override fun onEvent(event: GenericEvent) {
            if (event is T && filter(event)) {
                removeEventListener(this)
                cont.resume(event)
            }
        }
    }
    addEventListener(listener)
    cont.invokeOnCancellation { removeEventListener(listener) }
}
