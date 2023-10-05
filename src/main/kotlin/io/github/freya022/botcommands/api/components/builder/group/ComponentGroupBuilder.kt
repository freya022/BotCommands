package io.github.freya022.botcommands.api.components.builder.group

import io.github.freya022.botcommands.api.components.builder.IComponentBuilder
import io.github.freya022.botcommands.api.components.builder.ITimeoutableComponent
import io.github.freya022.botcommands.internal.components.ComponentType

abstract class ComponentGroupBuilder internal constructor(@get:JvmSynthetic internal val componentIds: List<Int>) :
    IComponentBuilder,
    ITimeoutableComponent {
    override val componentType: ComponentType = ComponentType.GROUP
}