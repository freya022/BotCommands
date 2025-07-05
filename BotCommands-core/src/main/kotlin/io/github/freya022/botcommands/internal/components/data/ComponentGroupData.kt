package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.data.timeout.ComponentTimeout
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal class ComponentGroupData internal constructor(
    override val internalId: Int,
    override val lifetimeType: LifetimeType,
    override val expiresAt: Instant?,
    override val resetTimeoutOnUseDuration: Duration?,
    override val timeout: ComponentTimeout?,
    internal val componentIds: List<Int>,
): ComponentData {
    override val componentType: ComponentType
        get() = ComponentType.GROUP
}