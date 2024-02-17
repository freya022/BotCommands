package io.github.freya022.botcommands.api.components.builder.group

import io.github.freya022.botcommands.api.components.IdentifiableComponent
import io.github.freya022.botcommands.api.components.builder.IEphemeralTimeoutableComponent
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.EphemeralTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.controller.ComponentController

class EphemeralComponentGroupBuilder internal constructor(
    componentController: ComponentController,
    components: Array<out IdentifiableComponent>,
    instanceRetriever: InstanceRetriever<EphemeralComponentGroupBuilder>
) : ComponentGroupBuilder<EphemeralComponentGroupBuilder>(componentController, components),
    IEphemeralTimeoutableComponent<EphemeralComponentGroupBuilder> by EphemeralTimeoutableComponentImpl(instanceRetriever) {

    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL
    override val instance: EphemeralComponentGroupBuilder = this

    init {
        instanceRetriever.instance = this
    }
}