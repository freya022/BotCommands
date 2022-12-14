package com.freya02.botcommands.internal.components.new

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.internal.components.ComponentHandler
import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.LifetimeType

internal abstract class ComponentData(
    val componentId: Int,
    val componentType: ComponentType,
    val lifetimeType: LifetimeType,
    val oneUse: Boolean,
    open val handler: ComponentHandler?,
    open val timeout: ComponentTimeout?,
    open val constraints: InteractionConstraints?,
    val groupId: Int?
)