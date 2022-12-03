package com.freya02.botcommands.internal.new_components.new

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.new_components.ComponentGroup
import com.freya02.botcommands.api.new_components.builder.ComponentBuilder
import com.freya02.botcommands.internal.new_components.builder.ComponentGroupBuilderImpl
import com.freya02.botcommands.internal.new_components.new.repositories.ComponentRepository
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.interactions.components.ActionComponent
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.coroutines.Continuation

@BService
internal class ComponentController(
    private val componentRepository: ComponentRepository,
    private val timeoutManager: ComponentTimeoutManager
) {
    private val continuationMap = hashMapOf<Int, MutableList<Continuation<*>>>()
    private val lock = ReentrantLock()

    fun createComponent(builder: ComponentBuilder): String {
        return componentRepository.createComponent(builder).also { id ->
            val timeout = builder.timeout ?: return@also
            timeoutManager.scheduleTimeout(id, timeout)
        }.toString()
    }

    suspend fun deleteComponent(component: ComponentData) {
        timeoutManager.cancelTimeout(component.componentId) //Only one timeout will be executed at most, as components inside groups aren't timeout-able
        componentRepository.deleteComponent(component).forEach { componentId ->
            timeoutManager.cancelTimeout(componentId)
        }
    }

    suspend fun insertGroup(group: ComponentGroupBuilderImpl): ComponentGroup {
        componentRepository.insertGroup(group).also { id ->
            val timeout = group.timeout ?: return@also
            timeoutManager.scheduleTimeout(id, timeout)
        }
        return ComponentGroup(group.componentIds)
    }

    suspend fun deleteComponentsById(ids: List<Int>) {
        componentRepository.deleteComponentsById(ids).forEach { componentId ->
            timeoutManager.cancelTimeout(componentId)
        }
    }

    fun removeContinuations(componentId: Int): List<Continuation<*>> {
        return continuationMap.remove(componentId) ?: emptyList()
    }

    private fun <T : GenericComponentInteractionCreateEvent> putContinuation(componentId: Int, cont: CancellableContinuation<T>) = lock.withLock {
        continuationMap.computeIfAbsent(componentId) { arrayListOf() }.add(cont)
    }

    //TODO Events returned to the continuations shares the localization context of the target handler,
    // it should probably share the same context as the slash command ?
    // What about localization context inside the coroutine context ?
    // This would be so java friendly...
    suspend fun <T : GenericComponentInteractionCreateEvent> awaitComponent(component: ActionComponent): T {
        return suspendCancellableCoroutine { continuation ->
            val componentId = component.id!!.toInt()
            putContinuation(componentId, continuation)

            continuation.invokeOnCancellation {
                removeContinuations(componentId)
            }
        }
    }
}