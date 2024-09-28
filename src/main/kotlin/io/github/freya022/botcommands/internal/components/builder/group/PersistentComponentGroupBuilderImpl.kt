package io.github.freya022.botcommands.internal.components.builder.group

import io.github.freya022.botcommands.api.components.IGroupHolder
import io.github.freya022.botcommands.api.components.builder.group.PersistentComponentGroupBuilder
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.builder.mixin.IPersistentTimeoutableComponentMixin
import io.github.freya022.botcommands.internal.components.builder.mixin.impl.PersistentTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentController

internal class PersistentComponentGroupBuilderImpl internal constructor(
    componentController: ComponentController,
    components: Array<out IGroupHolder>,
    instanceRetriever: InstanceRetriever<PersistentComponentGroupBuilder>
) : AbstractComponentGroupBuilder<PersistentComponentGroupBuilder>(componentController, components),
    PersistentComponentGroupBuilder,
    IPersistentTimeoutableComponentMixin<PersistentComponentGroupBuilder> by PersistentTimeoutableComponentImpl(instanceRetriever) {

    override val lifetimeType: LifetimeType get() = LifetimeType.PERSISTENT
    override val instance: PersistentComponentGroupBuilderImpl get() = this

    init {
        instanceRetriever.instance = this
    }
}