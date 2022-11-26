package com.freya02.botcommands.internal.new_components.new

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.ComponentType
import com.freya02.botcommands.internal.new_components.PersistentHandler

internal class PersistentComponentData(
    componentId: Int,
    componentType: ComponentType,
    lifetimeType: LifetimeType,
    oneUse: Boolean,
    override val handler: PersistentHandler,
    override val timeout: PersistentTimeout?,
    constraints: InteractionConstraints,
    groupId: Int?
) : AbstractComponentData(componentId, componentType, lifetimeType, oneUse, handler, timeout, constraints, groupId)