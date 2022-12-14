package com.freya02.botcommands.api.components.builder

import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.LifetimeType

interface IComponentBuilder {
    val componentType: ComponentType
    val lifetimeType: LifetimeType
}