package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.api.components.exceptions.ComponentCancellationException
import io.github.freya022.botcommands.api.components.exceptions.ComponentTimeoutException
import io.github.freya022.botcommands.api.components.exceptions.RemovedComponentException
import net.dv8tion.jda.api.interactions.components.ComponentInteraction

interface AwaitableComponent<T : ComponentInteraction> : IdentifiableComponent {
    /**
     * Suspends until the component is used and all checks passed, and returns the event.
     *
     * @throws ComponentTimeoutException If the timeout set in the component builder has been reached
     * @throws RemovedComponentException If the component was deleted while awaiting
     */
    @JvmSynthetic
    suspend fun await(): T
}

/**
 * Suspends until the component is used and all checks passed, and returns the event,
 * or `null` if the timeout has been reached, or the component was deleted.
 */
@JvmSynthetic
suspend fun <T : ComponentInteraction> AwaitableComponent<T>.awaitOrNull(): T? = try {
    await()
} catch (_: ComponentCancellationException) {
    null
}
