package com.freya02.botcommands.internal.new_components

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.api.components.builder.ComponentTimeoutInfo

internal class EphemeralComponentData(
    type: ComponentType,
    oneUse: Boolean,
    constraints: InteractionConstraints,
    timeoutInfo: ComponentTimeoutInfo?,
    internal val ephemeralHandlerId: Long
) : AbstractComponentData(type, oneUse, constraints, timeoutInfo)