package com.freya02.botcommands.api.components.builder.group

import com.freya02.botcommands.api.components.builder.IComponentBuilder
import com.freya02.botcommands.api.components.builder.ITimeoutableComponent
import com.freya02.botcommands.internal.components.ComponentType

abstract class ComponentGroupBuilder internal constructor(@get:JvmSynthetic internal val componentIds: List<Int>) :
    IComponentBuilder,
    ITimeoutableComponent {
    override val componentType: ComponentType = ComponentType.GROUP
}