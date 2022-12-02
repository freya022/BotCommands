package com.freya02.botcommands.internal.new_components.builder.select.ephemeral

import com.freya02.botcommands.api.new_components.builder.select.ephemeral.EphemeralEntitySelectBuilder
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.builder.select.EntitySelectBuilderImpl

internal class EphemeralEntitySelectBuilderImpl : EntitySelectBuilderImpl<EphemeralEntitySelectBuilder>(), EphemeralEntitySelectBuilder {
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL
}