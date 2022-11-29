package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.ComponentHandler
import com.freya02.botcommands.internal.new_components.ComponentType

interface ComponentBuilder<T : ComponentBuilder<T>> : ITimeoutableComponent, IConstrainableComponent<T>, IUniqueComponent<T> {
    val componentType: ComponentType
    val lifetimeType: LifetimeType
    val handler: ComponentHandler?
}