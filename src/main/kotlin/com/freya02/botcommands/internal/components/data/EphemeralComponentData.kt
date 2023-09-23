package com.freya02.botcommands.internal.components.data

import com.freya02.botcommands.api.components.data.InteractionConstraints
import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.EphemeralHandler
import com.freya02.botcommands.internal.components.LifetimeType

internal class EphemeralComponentData(
    componentId: Int,
    componentType: ComponentType,
    lifetimeType: LifetimeType,
    oneUse: Boolean,
    rateLimitGroup: String?,
    override val handler: EphemeralHandler<*>?,
    override val timeout: EphemeralTimeout?,
    constraints: InteractionConstraints,
    groupId: Int?
) : AbstractComponentData(componentId, componentType, lifetimeType, oneUse, rateLimitGroup, handler, timeout, constraints, groupId)