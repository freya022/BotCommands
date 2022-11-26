package com.freya02.botcommands.internal.new_components.new

import com.freya02.botcommands.api.components.InteractionConstraints
import com.freya02.botcommands.internal.data.LifetimeType
import com.freya02.botcommands.internal.new_components.ComponentHandler
import com.freya02.botcommands.internal.new_components.ComponentType

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