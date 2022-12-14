package com.freya02.botcommands.api.components.builder

import com.freya02.botcommands.internal.new_components.ComponentType
import com.freya02.botcommands.internal.new_components.LifetimeType

interface IComponentBuilder {
    val componentType: ComponentType
    val lifetimeType: LifetimeType
}