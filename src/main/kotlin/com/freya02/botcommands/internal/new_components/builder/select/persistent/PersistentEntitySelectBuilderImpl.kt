package com.freya02.botcommands.internal.new_components.builder.select.persistent

import com.freya02.botcommands.api.new_components.builder.select.persistent.PersistentEntitySelectBuilder
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.builder.select.EntitySelectBuilderImpl

internal class PersistentEntitySelectBuilderImpl : EntitySelectBuilderImpl<PersistentEntitySelectBuilder>(), PersistentEntitySelectBuilder {
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT
}