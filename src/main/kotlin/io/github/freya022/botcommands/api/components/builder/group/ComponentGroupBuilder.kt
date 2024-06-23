package io.github.freya022.botcommands.api.components.builder.group

import io.github.freya022.botcommands.api.components.ComponentGroup
import io.github.freya022.botcommands.api.components.IGroupHolder
import io.github.freya022.botcommands.api.components.builder.IComponentBuilder
import io.github.freya022.botcommands.api.components.builder.ITimeoutableComponent
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.IGroupHolderMixin
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import kotlinx.coroutines.runBlocking

sealed class ComponentGroupBuilder<T : ComponentGroupBuilder<T>>(
    private val componentController: ComponentController,
    private val components: Array<out IGroupHolder>,
) : IComponentBuilder,
    ITimeoutableComponent<T> {

    init {
        components.forEach {
            check(it.group == null) {
                "Component already has a group assigned: $it"
            }
        }
    }

    override val componentType: ComponentType = ComponentType.GROUP

    @get:JvmSynthetic
    internal val componentIds = components.map { it.internalId }

    fun build(): ComponentGroup = runBlocking { buildSuspend() }

    @JvmSynthetic
    @PublishedApi
    internal suspend fun buildSuspend(): ComponentGroup {
        return componentController.createGroup(this).also { group ->
            components.forEach {
                (it as IGroupHolderMixin).group = group
            }
        }
    }
}