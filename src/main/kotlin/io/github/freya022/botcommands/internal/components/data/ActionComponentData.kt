package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitReference
import io.github.freya022.botcommands.internal.components.handler.ComponentHandler

internal sealed interface ActionComponentData : ComponentData {
    val constraints: InteractionConstraints
    val singleUse: Boolean
    val filters: List<ComponentInteractionFilter<*>>
    val rateLimitReference: ComponentRateLimitReference?
    val handler: ComponentHandler?
    val group: ComponentGroupData?
}