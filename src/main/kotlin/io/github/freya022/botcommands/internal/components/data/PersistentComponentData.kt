package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.handler.PersistentHandler
import kotlinx.datetime.Instant

internal class PersistentComponentData(
    componentId: Int,
    componentType: ComponentType,
    lifetimeType: LifetimeType,
    expiresAt: Instant?,
    filters: List<ComponentInteractionFilter<*>>,
    oneUse: Boolean,
    rateLimitGroup: String?,
    override val handler: PersistentHandler?,
    override val timeout: PersistentTimeout?,
    constraints: InteractionConstraints,
    groupId: Int?
) : AbstractComponentData(componentId, componentType, lifetimeType, expiresAt, filters, oneUse, rateLimitGroup, handler, timeout, constraints, groupId)