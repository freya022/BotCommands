package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.data.ComponentTimeout
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.handler.ComponentHandler
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal sealed class AbstractComponentData(
    componentId: Int,
    componentType: ComponentType,
    lifetimeType: LifetimeType,
    expiresAt: Instant?,
    resetTimeoutOnUseDuration: Duration?,
    filters: List<ComponentInteractionFilter<*>>,
    oneUse: Boolean,
    rateLimitGroup: String?,
    handler: ComponentHandler?,
    timeout: ComponentTimeout?,
    final override val constraints: InteractionConstraints,
    group: ComponentGroupData?
): ComponentData(componentId, componentType, lifetimeType, expiresAt, resetTimeoutOnUseDuration, filters, oneUse, rateLimitGroup, handler, timeout, constraints, group)