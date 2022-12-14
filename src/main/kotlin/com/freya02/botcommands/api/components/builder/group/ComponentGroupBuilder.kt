package com.freya02.botcommands.api.components.builder.group

import com.freya02.botcommands.api.components.builder.IComponentBuilder
import com.freya02.botcommands.api.components.builder.ITimeoutableComponent
import com.freya02.botcommands.api.components.builder.IUniqueComponent
import com.freya02.botcommands.internal.new_components.ComponentType
import com.freya02.botcommands.internal.new_components.builder.UniqueComponentImpl

abstract class ComponentGroupBuilder internal constructor(@JvmSynthetic internal val componentIds: List<Int>) :
    IComponentBuilder,
    ITimeoutableComponent,
    IUniqueComponent by UniqueComponentImpl() {
    override val componentType: ComponentType = ComponentType.GROUP
}