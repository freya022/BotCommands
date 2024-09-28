package io.github.freya022.botcommands.internal.components.builder.group

import io.github.freya022.botcommands.api.components.ComponentGroup
import io.github.freya022.botcommands.api.components.IGroupHolder
import io.github.freya022.botcommands.api.components.builder.group.ComponentGroupBuilder
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.IGroupHolderMixin
import io.github.freya022.botcommands.internal.components.builder.mixin.IComponentBuilderMixin
import io.github.freya022.botcommands.internal.components.builder.mixin.ITimeoutableComponentMixin
import io.github.freya022.botcommands.internal.components.controller.ComponentController
import kotlinx.coroutines.runBlocking

@PublishedApi
internal sealed class AbstractComponentGroupBuilder<T : ComponentGroupBuilder<T>>(
    private val componentController: ComponentController,
    private val components: Array<out IGroupHolder>,
) : ComponentGroupBuilder<T>,
    IComponentBuilderMixin<T>,
    ITimeoutableComponentMixin<T> {

    init {
        components.forEach {
            check(it.group == null) {
                "Component already has a group assigned: $it"
            }
        }
    }

    override val componentType: ComponentType = ComponentType.GROUP

    internal val componentIds = components.map { it.internalId }

    override fun build(): ComponentGroup = runBlocking { buildSuspend() }

    @PublishedApi
    internal suspend fun buildSuspend(): ComponentGroup {
        return componentController.createGroup(this).also { group ->
            components.forEach {
                (it as IGroupHolderMixin).group = group
            }
        }
    }
}