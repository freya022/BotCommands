package com.freya02.botcommands.internal.new_components.new

import com.freya02.botcommands.api.core.annotations.BService
import com.freya02.botcommands.api.new_components.ComponentGroup
import com.freya02.botcommands.api.new_components.builder.ComponentBuilder
import com.freya02.botcommands.internal.new_components.builder.ComponentGroupBuilderImpl
import com.freya02.botcommands.internal.new_components.new.repositories.ComponentRepository

@BService
internal class ComponentController(
    private val componentRepository: ComponentRepository,
    private val timeoutManager: ComponentTimeoutManager
) {
    fun createComponent(builder: ComponentBuilder<*>): String {
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
}