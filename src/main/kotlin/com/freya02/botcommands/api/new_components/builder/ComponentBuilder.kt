package com.freya02.botcommands.api.new_components.builder

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.ComponentHandler
import com.freya02.botcommands.internal.new_components.ComponentType

interface ComponentBuilder<T : ComponentBuilder<T>> : ITimeoutableComponent, IUniqueComponent<T> {
    val componentType: ComponentType
    val lifetimeType: LifetimeType
    val constraints: InteractionConstraints
    val handler: ComponentHandler?
}