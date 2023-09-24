package com.freya02.botcommands.internal.components.data

import com.freya02.botcommands.api.components.data.ComponentTimeout
import com.freya02.botcommands.api.components.data.InteractionConstraints
import com.freya02.botcommands.internal.components.ComponentHandler
import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.LifetimeType

internal sealed class AbstractComponentData(
    componentId: Int,
    componentType: ComponentType,
    lifetimeType: LifetimeType,
    oneUse: Boolean,
    rateLimitGroup: String?,
    handler: ComponentHandler?,
    timeout: ComponentTimeout?,
    final override val constraints: InteractionConstraints,
    groupId: Int?
): ComponentData(componentId, componentType, lifetimeType, oneUse, rateLimitGroup, handler, timeout, constraints, groupId)