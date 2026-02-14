package io.github.freya022.botcommands.internal.components.controller.local

import io.github.freya022.botcommands.api.components.annotations.RequiresLocalComponents
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.components.builder.group.AbstractComponentGroupBuilder
import io.github.freya022.botcommands.internal.components.builder.mixin.BaseComponentBuilderMixin
import io.github.freya022.botcommands.internal.components.controller.ComponentContinuationManager
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import io.github.freya022.botcommands.internal.components.controller.ComponentTimeoutManager
import io.github.freya022.botcommands.internal.components.data.ComponentData
import io.github.freya022.botcommands.internal.components.data.ComponentGroupData
import io.github.freya022.botcommands.internal.components.repositories.local.LocalComponentRepository
import kotlinx.datetime.Instant

@BService
@RequiresLocalComponents
internal class LocalComponentControllerImpl(
    override val context: BContext,
    override val continuationManager: ComponentContinuationManager,
    override val timeoutManager: ComponentTimeoutManager,
    private val componentRepository: LocalComponentRepository,
) : ComponentController() {

    override suspend fun createComponent(builder: BaseComponentBuilderMixin<*>): ComponentData {
        return componentRepository.createComponent(builder)
    }

    override suspend fun getComponent(componentId: Int): ComponentData? {
        return componentRepository.getComponent(componentId)
    }

    override suspend fun resetExpiration(internalId: Int): Instant {
        return componentRepository.resetExpiration(internalId)
    }

    override suspend fun createGroupData(builder: AbstractComponentGroupBuilder<*>): ComponentGroupData {
        return componentRepository.insertGroup(builder)
    }

    override suspend fun deleteComponentsById(ids: Collection<Int>, throwTimeouts: Boolean) {
        componentRepository.deleteComponentsById(ids).forEach { (componentId) ->
            timeoutManager.removeTimeouts(componentId, throwTimeouts)
        }
    }
}
