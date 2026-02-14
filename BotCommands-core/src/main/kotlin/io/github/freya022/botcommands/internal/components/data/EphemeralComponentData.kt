package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitReference
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.data.timeout.EphemeralTimeout
import io.github.freya022.botcommands.internal.components.handler.EphemeralHandler
import kotlin.time.Duration
import kotlin.time.Instant

internal class EphemeralComponentData(
    override val internalId: Int,
    override val componentType: ComponentType,
    override val expiresAt: Instant?,
    override val resetTimeoutOnUseDuration: Duration?,
    override val filters: List<ComponentInteractionFilter>,
    override val singleUse: Boolean,
    override val rateLimitReference: ComponentRateLimitReference?,
    override val handler: EphemeralHandler<*>?,
    override val timeout: EphemeralTimeout?,
    override val constraints: InteractionConstraints,
    override val group: ComponentGroupData?
) : ActionComponentData {
    override val lifetimeType: LifetimeType
        get() = LifetimeType.EPHEMERAL

    override fun withGroup(group: ComponentGroupData): EphemeralComponentData {
        require(this.group == null) {
            "Attempted to override a group with another"
        }

        return EphemeralComponentData(
            internalId,
            componentType,
            expiresAt,
            resetTimeoutOnUseDuration,
            filters,
            singleUse,
            rateLimitReference,
            handler,
            timeout,
            constraints,
            group
        )
    }

    override fun withExpiration(expiresAt: Instant): EphemeralComponentData {
        return EphemeralComponentData(
            internalId,
            componentType,
            expiresAt,
            resetTimeoutOnUseDuration,
            filters,
            singleUse,
            rateLimitReference,
            handler,
            timeout,
            constraints,
            group
        )
    }
}
