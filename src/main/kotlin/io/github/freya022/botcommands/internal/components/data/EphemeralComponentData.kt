package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.handler.EphemeralHandler
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal class EphemeralComponentData(
    override val internalId: Int,
    override val componentType: ComponentType,
    override val expiresAt: Instant?,
    override val resetTimeoutOnUseDuration: Duration?,
    override val filters: List<ComponentInteractionFilter<*>>,
    override val singleUse: Boolean,
    override val rateLimitGroup: String?,
    override val handler: EphemeralHandler<*>?,
    override val timeout: EphemeralTimeout?,
    override val constraints: InteractionConstraints,
    override val group: ComponentGroupData?
) : ActionComponentData {
    override val lifetimeType: LifetimeType
        get() = LifetimeType.EPHEMERAL
}