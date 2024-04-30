package io.github.freya022.botcommands.api.components

import kotlinx.coroutines.TimeoutCancellationException
import net.dv8tion.jda.api.interactions.components.ComponentInteraction

sealed interface AwaitableComponent<T : ComponentInteraction> {
    /**
     * Suspends until the component is used and all checks passed, and returns the event.
     *
     * @throws TimeoutCancellationException If the timeout set in the component builder has been reached
     */
    @JvmSynthetic
    suspend fun await(): T
}

/**
 * Suspends until the component is used and all checks passed, and returns the event,
 * or `null` if the timeout has been reached.
 */
@JvmSynthetic
suspend fun <T : ComponentInteraction> AwaitableComponent<T>.awaitOrNull(): T? = try {
    await()
} catch (e: TimeoutCancellationException) {
    null
}