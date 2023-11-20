package io.github.freya022.botcommands.api.components.builder.group

import io.github.freya022.botcommands.api.components.builder.IPersistentTimeoutableComponent
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.builder.PersistentTimeoutableComponentImpl

class PersistentComponentGroupBuilder internal constructor(
    componentIds: List<Int>
) : ComponentGroupBuilder<PersistentComponentGroupBuilder>(componentIds),
    IPersistentTimeoutableComponent<PersistentComponentGroupBuilder> by PersistentTimeoutableComponentImpl() {

    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT
}