package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.api.components.ComponentInteractionFilter
import io.github.freya022.botcommands.api.components.data.InteractionConstraints
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitReference
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.data.timeout.PersistentTimeout
import io.github.freya022.botcommands.internal.components.handler.PersistentHandler
import kotlin.time.Duration
import kotlin.time.Instant

internal class PersistentComponentData(
    override val internalId: Int,
    override val componentType: ComponentType,
    override val expiresAt: Instant?,
    override val resetTimeoutOnUseDuration: Duration?,
    override val filters: List<ComponentInteractionFilter>,
    override val singleUse: Boolean,
    override val rateLimitReference: ComponentRateLimitReference?,
    override val handler: PersistentHandler?,
    override val timeout: PersistentTimeout?,
    override val constraints: InteractionConstraints,
    override val group: ComponentGroupData?
) : ActionComponentData {
    override val lifetimeType: LifetimeType
        get() = LifetimeType.PERSISTENT

    override fun withGroup(group: ComponentGroupData): PersistentComponentData {
        require(this.group == null) {
            "Attempted to override a group with another"
        }

        return PersistentComponentData(
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

    override fun withExpiration(expiresAt: Instant): PersistentComponentData {
        return PersistentComponentData(
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
