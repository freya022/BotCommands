package io.github.freya022.botcommands.api.components.builder.group

import io.github.freya022.botcommands.api.components.builder.IComponentBuilder
import io.github.freya022.botcommands.api.components.builder.ITimeoutableComponent
import io.github.freya022.botcommands.internal.components.ComponentType

sealed class ComponentGroupBuilder<T : ComponentGroupBuilder<T>>(@get:JvmSynthetic internal val componentIds: List<Int>) :
    IComponentBuilder,
    ITimeoutableComponent<T> {
    override val componentType: ComponentType = ComponentType.GROUP
}