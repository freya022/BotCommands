package com.freya02.botcommands.internal.new_components.new

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.internal.new_components.ComponentType
import com.freya02.botcommands.internal.new_components.EphemeralHandler
import com.freya02.botcommands.internal.new_components.LifetimeType

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