package com.freya02.botcommands.internal.new_components.new

import com.freya02.botcommands.api.core.annotations.ConditionalService
import com.freya02.botcommands.api.new_components.ComponentGroup
import com.freya02.botcommands.api.new_components.IdentifiableComponent
import com.freya02.botcommands.api.new_components.builder.ComponentBuilder
import com.freya02.botcommands.api.new_components.builder.group.ComponentGroupBuilder
import com.freya02.botcommands.internal.new_components.new.repositories.ComponentRepository
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@ConditionalService(dependencies = [ComponentRepository::class])
internal class ComponentController(
    private val componentRepository: ComponentRepository,
    private val timeoutManager: ComponentTimeoutManager
) {
    private val continuationMap = hashMapOf<Int, MutableList<CancellableContinuation<*>>>()
    private val lock = ReentrantLock()

    init {
        runBlocking { componentRepository.scheduleExistingTimeouts(timeoutManager) }
    }

    fun createComponent(builder: ComponentBuilder): String {
        return componentRepository.createComponent(builder).also { id ->
            val timeout = builder.timeout ?: return@also
            timeoutManager.scheduleTimeout(id, timeout)
        }.toString()
    }

    suspend fun deleteComponent(component: ComponentData, isTimeout: Boolean = false) {
        timeoutManager.cancelTimeout(component.componentId) //Only one timeout will be executed at most, as components inside groups aren't timeout-able
        componentRepository.deleteComponent(component).forEach { componentId ->
            timeoutManager.cancelTimeout(componentId)
            if (isTimeout) {
                timeoutManager.throwTimeouts(componentId)
            }
        }
    }

    suspend fun insertGroup(group: ComponentGroupBuilder): ComponentGroup {
        return componentRepository.insertGroup(group).also { id ->
            val timeout = group.timeout ?: return@also
            timeoutManager.scheduleTimeout(id, timeout)
        }.let { id -> ComponentGroup(this, id.toString()) }
    }

    suspend fun deleteComponentsById(ids: List<Int>) {
        componentRepository.deleteComponentsById(ids).forEach { componentId ->
            timeoutManager.cancelTimeout(componentId)
        }
    }

    fun removeContinuations(componentId: Int): List<CancellableContinuation<*>> = lock.withLock {
        return continuationMap.remove(componentId) ?: emptyList()
    }

    private fun <T : GenericComponentInteractionCreateEvent> putContinuation(componentId: Int, cont: CancellableContinuation<T>) = lock.withLock {
        continuationMap.computeIfAbsent(componentId) { arrayListOf() }.add(cont)
    }

    //TODO Events returned to the continuations shares the localization context of the target handler,
    // it should probably share the same context as the slash command ?
    // What about localization context inside the coroutine context ?
    // This would be so java friendly...
    suspend fun <T : GenericComponentInteractionCreateEvent> awaitComponent(component: IdentifiableComponent): T {
        return suspendCancellableCoroutine { continuation ->
            val componentId = component.getId()!!.toInt()
            putContinuation(componentId, continuation)

            continuation.invokeOnCancellation {
                removeContinuations(componentId)
            }
        }
    }
}