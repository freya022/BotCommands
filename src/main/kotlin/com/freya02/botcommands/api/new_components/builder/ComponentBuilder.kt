package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.internal.new_components.ComponentType
import com.freya02.botcommands.internal.new_components.LifetimeType

interface ComponentBuilder : ITimeoutableComponent, IActionableComponent, IConstrainableComponent, IUniqueComponent {
    val componentType: ComponentType
    val lifetimeType: LifetimeType
}