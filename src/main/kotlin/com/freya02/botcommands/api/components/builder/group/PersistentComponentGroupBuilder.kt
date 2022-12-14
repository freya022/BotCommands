package com.freya02.botcommands.api.components.builder.group

import com.freya02.botcommands.api.components.builder.IPersistentTimeoutableComponent
import com.freya02.botcommands.internal.components.LifetimeType
import com.freya02.botcommands.internal.components.builder.PersistentTimeoutableComponentImpl

class PersistentComponentGroupBuilder internal constructor(componentIds: List<Int>) : ComponentGroupBuilder(componentIds),
    IPersistentTimeoutableComponent by PersistentTimeoutableComponentImpl() {

    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT
}