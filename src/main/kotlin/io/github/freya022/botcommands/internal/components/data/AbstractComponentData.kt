package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.data.ComponentTimeout
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.handler.ComponentHandler

internal sealed class AbstractComponentData(
    componentId: Int,
    componentType: ComponentType,
    lifetimeType: LifetimeType,
    filters: List<ComponentInteractionFilter<*>>,
    oneUse: Boolean,
    rateLimitGroup: String?,
    handler: ComponentHandler?,
    timeout: ComponentTimeout?,
    final override val constraints: InteractionConstraints,
    groupId: Int?
): ComponentData(componentId, componentType, lifetimeType, filters, oneUse, rateLimitGroup, handler, timeout, constraints, groupId)