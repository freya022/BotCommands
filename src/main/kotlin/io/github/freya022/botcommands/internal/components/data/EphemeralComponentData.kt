package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.handler.EphemeralHandler
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal class EphemeralComponentData(
    componentId: Int,
    componentType: ComponentType,
    lifetimeType: LifetimeType,
    expiresAt: Instant?,
    resetTimeoutOnUseDuration: Duration?,
    filters: List<ComponentInteractionFilter<*>>,
    oneUse: Boolean,
    rateLimitGroup: String?,
    override val handler: EphemeralHandler<*>?,
    override val timeout: EphemeralTimeout?,
    constraints: InteractionConstraints,
    group: ComponentGroupData?
) : AbstractComponentData(componentId, componentType, lifetimeType, expiresAt, resetTimeoutOnUseDuration, filters, oneUse, rateLimitGroup, handler, timeout, constraints, group)