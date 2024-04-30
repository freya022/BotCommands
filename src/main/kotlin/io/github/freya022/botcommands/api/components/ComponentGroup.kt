package io.github.freya022.botcommands.api.components

import io.github.freya022.botcommands.internal.components.controller.ComponentController
import kotlinx.coroutines.TimeoutCancellationException
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent

class ComponentGroup internal constructor(
    private val componentController: ComponentController,
    override val internalId: Int
) : IdentifiableComponent, AwaitableComponent<GenericComponentInteractionCreateEvent> {
    @JvmSynthetic
    override suspend fun await(): GenericComponentInteractionCreateEvent = componentController.awaitComponent(this)
}

/**
 * Suspends until the component is used and all checks passed, and returns the event.
 *
 * @throws TimeoutCancellationException If the timeout set in the component builder has been reached
 * @throws ClassCastException If the received event cannot be cast to the requested type
 */
@JvmSynthetic
@Suppress("UNCHECKED_CAST")
suspend fun <T : GenericComponentInteractionCreateEvent> ComponentGroup.awaitAny(): T = await() as T

/**
 * Suspends until the component is used and all checks passed, and returns the event,
 * or `null` if the timeout has been reached.
 *
 * @throws ClassCastException If the received event cannot be cast to the requested type
 */
@JvmSynthetic
@Suppress("UNCHECKED_CAST")
suspend fun <T : GenericComponentInteractionCreateEvent> ComponentGroup.awaitAnyOrNull(): T? = awaitOrNull() as T?