package com.freya02.botcommands.internal.components.data

import com.freya02.botcommands.api.components.data.ComponentTimeout
import com.freya02.botcommands.api.components.data.InteractionConstraints
import com.freya02.botcommands.internal.components.ComponentHandler
import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.LifetimeType

internal abstract class AbstractComponentData(
    componentId: Int,
    componentType: ComponentType,
    lifetimeType: LifetimeType,
    oneUse: Boolean,
    handler: ComponentHandler?,
    timeout: ComponentTimeout?,
    final override val constraints: InteractionConstraints
): ComponentData(componentId, componentType, lifetimeType, oneUse, handler, timeout, constraints)