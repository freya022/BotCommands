package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.api.components.data.ComponentTimeout
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.internal.components.ComponentHandler
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType

internal sealed class AbstractComponentData(
    componentId: Int,
    componentType: ComponentType,
    lifetimeType: LifetimeType,
    oneUse: Boolean,
    rateLimitGroup: String?,
    handler: ComponentHandler?,
    timeout: ComponentTimeout?,
    final override val constraints: InteractionConstraints,
    groupId: Int?
): ComponentData(componentId, componentType, lifetimeType, oneUse, rateLimitGroup, handler, timeout, constraints, groupId)