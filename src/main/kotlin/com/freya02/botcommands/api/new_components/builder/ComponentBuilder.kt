package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.ComponentType

interface ComponentBuilder : ITimeoutableComponent, IActionableComponent, IConstrainableComponent, IUniqueComponent {
    val componentType: ComponentType
    val lifetimeType: LifetimeType
}