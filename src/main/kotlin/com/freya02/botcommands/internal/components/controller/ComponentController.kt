package com.freya02.botcommands.internal.components.controller

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.ratelimit.RateLimitContainer
import com.freya02.botcommands.api.commands.ratelimit.annotations.RateLimitDeclaration
import com.freya02.botcommands.api.components.ComponentGroup
import com.freya02.botcommands.api.components.IdentifiableComponent
import com.freya02.botcommands.api.components.builder.BaseComponentBuilder
import com.freya02.botcommands.api.components.builder.group.ComponentGroupBuilder
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.service.annotations.Dependencies
import com.freya02.botcommands.api.core.service.lazy
import com.freya02.botcommands.api.core.utils.simpleNestedName
import com.freya02.botcommands.internal.components.data.ComponentData
import com.freya02.botcommands.internal.components.repositories.ComponentRepository
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

@BService
@Dependencies(ComponentRepository::class)
internal class ComponentController(
    context: BContext,
    private val componentRepository: ComponentRepository,
    private val timeoutManager: ComponentTimeoutManager
) {
    // This service might be used in classes that use components and also declare rate limiters
    private val rateLimitContainer: RateLimitContainer by context.serviceContainer.lazy()

    private val continuationMap = hashMapOf<Int, MutableList<CancellableContinuation<GenericComponentInteractionCreateEvent>>>()
    private val lock = ReentrantLock()

    init {
        runBlocking { componentRepository.scheduleExistingTimeouts(timeoutManager) }
    }

    fun createComponent(builder: BaseComponentBuilder): String {
        builder.rateLimitGroup?.let { rateLimitGroup ->
            require(rateLimitGroup in rateLimitContainer) {
                "Rate limit group '$rateLimitGroup' was not registered using @${RateLimitDeclaration::class.simpleNestedName}"
            }
        }

        return componentRepository.createComponent(builder).also { id ->
            val timeout = builder.timeout ?: return@also
            timeoutManager.scheduleTimeout(id, timeout)
        }.toString()
    }

    suspend fun deleteComponent(component: ComponentData, isTimeout: Boolean = false) {
        //Only one timeout will be executed at most, as components inside groups aren't timeout-able
        componentRepository.deleteComponent(component.componentId).forEach { componentId ->
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

    private fun putContinuation(componentId: Int, cont: CancellableContinuation<GenericComponentInteractionCreateEvent>) = lock.withLock {
        continuationMap.computeIfAbsent(componentId) { arrayListOf() }.add(cont)
    }

    @Suppress("UNCHECKED_CAST")
    internal suspend fun <T : GenericComponentInteractionCreateEvent> awaitComponent(component: IdentifiableComponent): T {
        return suspendCancellableCoroutine { continuation ->
            val componentId = component.getId()!!.toInt()
            putContinuation(componentId, continuation)

            continuation.invokeOnCancellation {
                removeContinuations(componentId)
            }
        } as T
    }
}