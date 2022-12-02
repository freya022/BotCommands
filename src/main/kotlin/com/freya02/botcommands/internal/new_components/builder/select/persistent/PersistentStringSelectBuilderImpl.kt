package com.freya02.botcommands.internal.new_components.builder.select.persistent

import com.freya02.botcommands.api.new_components.builder.select.persistent.PersistentStringSelectBuilder
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.builder.select.StringSelectBuilderImpl

internal class PersistentStringSelectBuilderImpl : StringSelectBuilderImpl<PersistentStringSelectBuilder>(), PersistentStringSelectBuilder {
    override val lifetimeType: LifetimeType = LifetimeType.PERSISTENT
}