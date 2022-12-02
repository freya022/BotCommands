package com.freya02.botcommands.internal.new_components.builder.select.ephemeral

import com.freya02.botcommands.api.new_components.builder.select.ephemeral.EphemeralStringSelectBuilder
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.builder.select.StringSelectBuilderImpl

internal class EphemeralStringSelectBuilderImpl : StringSelectBuilderImpl<EphemeralStringSelectBuilder>(), EphemeralStringSelectBuilder {
    override val lifetimeType: LifetimeType = LifetimeType.EPHEMERAL
}