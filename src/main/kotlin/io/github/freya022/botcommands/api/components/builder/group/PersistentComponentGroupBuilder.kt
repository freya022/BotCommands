package io.github.freya022.botcommands.api.components.builder.group

import io.github.freya022.botcommands.api.components.IdentifiableComponent
import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.builder.PersistentTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.controller.ComponentController

class PersistentComponentGroupBuilder internal constructor(
    componentController: ComponentController,
    components: Array<out IdentifiableComponent>,
    instanceRetriever: InstanceRetriever<PersistentComponentGroupBuilder>
) : ComponentGroupBuilder<PersistentComponentGroupBuilder>(componentController, components),
    IPersistentTimeoutableComponent<PersistentComponentGroupBuilder> by PersistentTimeoutableComponentImpl(instanceRetriever) {

    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT
    override val instance: PersistentComponentGroupBuilder = this

    init {
        instanceRetriever.instance = this
    }
}