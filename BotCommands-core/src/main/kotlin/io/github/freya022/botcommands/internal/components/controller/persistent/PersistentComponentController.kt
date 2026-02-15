package io.github.freya022.botcommands.internal.components.controller.persistent

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.components.annotations.RequiresPersistentComponents
import io.github.freya022.botcommands.internal.components.builder.group.AbstractComponentGroupBuilder
import io.github.freya022.botcommands.internal.components.builder.mixin.BaseComponentBuilderMixin
import io.github.freya022.botcommands.internal.components.controller.ComponentContinuationManager
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.components.controller.ComponentTimeoutManager
import io.github.freya022.botcommands.internal.components.data.ComponentData
import io.github.freya022.botcommands.internal.components.data.ComponentGroupData
import io.github.freya022.botcommands.internal.components.handler.persistent.EphemeralComponentHandlers
import io.github.freya022.botcommands.internal.components.repositories.persistent.PersistentComponentRepository
import io.github.freya022.botcommands.internal.components.timeout.persistent.EphemeralTimeoutHandlers
import io.github.freya022.botcommands.internal.utils.reference
import io.github.freya022.botcommands.internal.utils.throwInternal
import kotlinx.coroutines.runBlocking
import kotlin.time.Instant

@BService
@RequiresPersistentComponents
internal class PersistentComponentController(
    override val context: BContext,
    override val continuationManager: ComponentContinuationManager,
    override val timeoutManager: ComponentTimeoutManager,
    private val componentRepository: PersistentComponentRepository,
    private val ephemeralComponentHandlers: EphemeralComponentHandlers,
    private val ephemeralTimeoutHandlers: EphemeralTimeoutHandlers,
) : ComponentController() {

    init {
        runBlocking {
            removeEphemeralComponents()
            scheduleExistingTimeouts()
        }
    }

    private suspend fun removeEphemeralComponents() {
        val removedComponents = componentRepository.removeEphemeralComponents()
        logger.debug { "Removed $removedComponents ephemeral components" }
    }

    private suspend fun scheduleExistingTimeouts() {
        componentRepository
            .getPersistentComponentTimeouts()
            .forEach {
                timeoutManager.scheduleTimeout(it.componentId, it.instant)
            }
    }

    override suspend fun createComponent(builder: BaseComponentBuilderMixin<*>): ComponentData {
        return componentRepository.createComponent(builder)
    }

    override suspend fun getComponent(componentId: Int): ComponentData? {
        return componentRepository.getComponent(componentId)
    }

    override suspend fun resetExpiration(internalId: Int): Instant {
        return componentRepository.resetExpiration(internalId)
            ?: throwInternal("New expiration timestamp is null despite ${ComponentData::resetTimeoutOnUseDuration.reference} being non-null")
    }

    override suspend fun createGroupData(builder: AbstractComponentGroupBuilder<*>): ComponentGroupData {
        return componentRepository.insertGroup(builder)
    }

    override suspend fun deleteComponentsById(ids: Collection<Int>, throwTimeouts: Boolean) {
        componentRepository.deleteComponentsById(ids).forEach { (componentId, ephemeralComponentHandlerId, ephemeralTimeoutHandlerId) ->
            ephemeralComponentHandlerId?.let { ephemeralComponentHandlers.remove(it) }
            ephemeralTimeoutHandlerId?.let { ephemeralTimeoutHandlers.remove(it) }
            timeoutManager.removeTimeouts(componentId, throwTimeouts)
        }
    }
}
