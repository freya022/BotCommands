package io.github.freya022.botcommands.internal.components.builder.group

import io.github.freya022.botcommands.api.components.IGroupHolder
import io.github.freya022.botcommands.api.components.builder.group.EphemeralComponentGroupBuilder
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.EphemeralTimeoutableComponentImpl
import io.github.freya022.botcommands.internal.components.builder.IEphemeralTimeoutableComponentMixin
import io.github.freya022.botcommands.internal.components.builder.InstanceRetriever
import io.github.freya022.botcommands.internal.components.controller.ComponentController

internal class EphemeralComponentGroupBuilderImpl internal constructor(
    componentController: ComponentController,
    components: Array<out IGroupHolder>,
    instanceRetriever: InstanceRetriever<EphemeralComponentGroupBuilder>
) : AbstractComponentGroupBuilder<EphemeralComponentGroupBuilder>(componentController, components),
    EphemeralComponentGroupBuilder,
    IEphemeralTimeoutableComponentMixin<EphemeralComponentGroupBuilder> by EphemeralTimeoutableComponentImpl(instanceRetriever) {

    override val lifetimeType: LifetimeType get() = LifetimeType.EPHEMERAL
    override val instance: EphemeralComponentGroupBuilder get() = this

    init {
        instanceRetriever.instance = this
    }
}