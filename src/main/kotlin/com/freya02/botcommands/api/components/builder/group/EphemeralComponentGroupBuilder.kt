package com.freya02.botcommands.api.components.builder.group

import com.freya02.botcommands.api.components.builder.IEphemeralTimeoutableComponent
import com.freya02.botcommands.internal.new_components.LifetimeType
import com.freya02.botcommands.internal.new_components.builder.EphemeralTimeoutableComponentImpl

class EphemeralComponentGroupBuilder internal constructor(componentIds: List<Int>) : ComponentGroupBuilder(componentIds),
    IEphemeralTimeoutableComponent by EphemeralTimeoutableComponentImpl() {

    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL
}