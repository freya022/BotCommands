package com.freya02.botcommands.internal.components.data

import com.freya02.botcommands.api.components.data.InteractionConstraints
import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.LifetimeType
import com.freya02.botcommands.internal.components.PersistentHandler

internal class PersistentComponentData(
    componentId: Int,
    componentType: ComponentType,
    lifetimeType: LifetimeType,
    oneUse: Boolean,
    rateLimitGroup: String?,
    override val handler: PersistentHandler?,
    override val timeout: PersistentTimeout?,
    constraints: InteractionConstraints,
    groupId: Int?
) : AbstractComponentData(componentId, componentType, lifetimeType, oneUse, rateLimitGroup, handler, timeout, constraints, groupId)