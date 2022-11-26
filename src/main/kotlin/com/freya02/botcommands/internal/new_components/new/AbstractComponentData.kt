package com.freya02.botcommands.internal.new_components.new

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.ComponentHandler
import com.freya02.botcommands.internal.new_components.ComponentType

internal abstract class AbstractComponentData(
    componentId: Int,
    componentType: ComponentType,
    lifetimeType: LifetimeType,
    oneUse: Boolean,
    handler: ComponentHandler?,
    timeout: ComponentTimeout?,
    override val constraints: InteractionConstraints,
    groupId: Int?
): ComponentData(componentId, componentType, lifetimeType, oneUse, handler, timeout, constraints, groupId)