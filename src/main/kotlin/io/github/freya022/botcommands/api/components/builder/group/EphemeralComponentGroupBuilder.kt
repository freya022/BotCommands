package io.github.freya022.botcommands.api.components.builder.group

import io.github.freya022.botcommands.api.components.builder.IEphemeralTimeoutableComponent
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.EphemeralTimeoutableComponentImpl

class EphemeralComponentGroupBuilder internal constructor(
    componentIds: List<Int>
) : ComponentGroupBuilder<EphemeralComponentGroupBuilder>(componentIds),
    IEphemeralTimeoutableComponent<EphemeralComponentGroupBuilder> by EphemeralTimeoutableComponentImpl() {

    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL
}