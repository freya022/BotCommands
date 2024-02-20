package io.github.freya022.botcommands.api.components

import kotlinx.coroutines.TimeoutCancellationException
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

interface IdentifiableComponent {
    val internalId: Int

    /**
     * Suspends until the component is used and all checks passed, and returns the event.
     *
     * @throws TimeoutCancellationException If the timeout set in the component builder has been reached
     */
    @JvmSynthetic
    suspend fun await(): GenericComponentInteractionCreateEvent
}

/**
 * Suspends until the component is used and all checks passed, and returns the event,
 * or `null` if the timeout has been reached.
 */
@JvmSynthetic
suspend fun IdentifiableComponent.awaitOrNull(): GenericComponentInteractionCreateEvent? = try {
    await()
} catch (e: TimeoutCancellationException) {
    null
}