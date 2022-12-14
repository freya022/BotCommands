package com.freya02.botcommands.internal.components.new

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.EphemeralHandler
import com.freya02.botcommands.internal.components.LifetimeType

internal class EphemeralComponentData(
    componentId: Int,
    componentType: ComponentType,
    lifetimeType: LifetimeType,
    oneUse: Boolean,
    override val handler: EphemeralHandler<*>,
    override val timeout: EphemeralTimeout?,
    constraints: InteractionConstraints,
    groupId: Int?
) : AbstractComponentData(componentId, componentType, lifetimeType, oneUse, handler, timeout, constraints, groupId)