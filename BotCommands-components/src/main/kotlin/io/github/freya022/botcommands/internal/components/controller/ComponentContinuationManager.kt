package io.github.freya022.botcommands.internal.components.controller

import io.github.freya022.botcommands.api.components.IdentifiableComponent
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.components.data.ActionComponentData
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.resume

private typealias ComponentInteractionEventContinuation = CancellableContinuation<GenericComponentInteractionCreateEvent>

@BService
@RequiresComponents
internal class ComponentContinuationManager internal constructor() {
    private val continuationMap = hashMapOf<Int, MutableList<ComponentInteractionEventContinuation>>()
    private val lock = ReentrantLock()

    @Suppress("UNCHECKED_CAST")
    internal suspend fun <T : GenericComponentInteractionCreateEvent> awaitComponent(component: IdentifiableComponent): T {
        return suspendCancellableCoroutine { continuation ->
            val componentId = component.internalId
            putContinuation(componentId, continuation)

            continuation.invokeOnCancellation {
                removeContinuations(componentId)
            }
        } as T
    }

    private fun putContinuation(componentId: Int, cont: ComponentInteractionEventContinuation) = lock.withLock {
        continuationMap.computeIfAbsent(componentId) { arrayListOf() }.add(cont)
    }

    internal fun resumeCoroutines(component: ActionComponentData, event: GenericComponentInteractionCreateEvent) = lock.withLock {
        component.group?.let { group ->
            removeContinuations(group.internalId).forEach {
                it.resume(event)
            }
        }

        removeContinuations(component.internalId).forEach {
            it.resume(event)
        }
    }

    internal fun removeContinuations(componentId: Int): List<ComponentInteractionEventContinuation> = lock.withLock {
        return continuationMap.remove(componentId) ?: emptyList()
    }
}