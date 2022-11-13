package com.freya02.botcommands.internal.new_components

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.api.new_components.ComponentTimeoutInfo

internal class PersistentComponentData(
    type: ComponentType,
    oneUse: Boolean,
    constraints: InteractionConstraints,
    timeoutInfo: ComponentTimeoutInfo?,
    internal val persistentHandler: PersistentHandler
) : AbstractComponentData(type, oneUse, constraints, timeoutInfo)