package com.freya02.botcommands.internal.components.new

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.internal.components.ComponentHandler
import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.LifetimeType
import com.freya02.botcommands.internal.components.data.ComponentTimeout

internal abstract class AbstractComponentData(
    componentId: Int,
    componentType: ComponentType,
    lifetimeType: LifetimeType,
    oneUse: Boolean,
    handler: ComponentHandler?,
    timeout: ComponentTimeout?,
    final override val constraints: InteractionConstraints,
    groupId: Int?
): ComponentData(componentId, componentType, lifetimeType, oneUse, handler, timeout, constraints, groupId)