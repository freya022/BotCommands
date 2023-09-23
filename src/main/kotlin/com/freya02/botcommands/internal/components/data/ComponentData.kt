package com.freya02.botcommands.internal.components.data

import com.freya02.botcommands.api.components.data.ComponentTimeout
import com.freya02.botcommands.api.components.data.InteractionConstraints
import com.freya02.botcommands.internal.components.ComponentHandler
import com.freya02.botcommands.internal.components.ComponentType
import com.freya02.botcommands.internal.components.LifetimeType

internal sealed class ComponentData(
    val componentId: Int,
    val componentType: ComponentType,
    val lifetimeType: LifetimeType,
    val oneUse: Boolean,
    val rateLimitGroup: String?,
    open val handler: ComponentHandler?,
    open val timeout: ComponentTimeout?,
    open val constraints: InteractionConstraints?,
    val groupId: Int?
)