package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.api.components.data.ComponentTimeout
import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import kotlinx.datetime.Instant
import kotlin.time.Duration

internal class ComponentGroupData internal constructor(
    groupId: Int,
    oneUse: Boolean,
    expiresAt: Instant?,
    resetTimeoutOnUseDuration: Duration?,
    timeout: ComponentTimeout?,
    internal val componentIds: List<Int>
): ComponentData(groupId, ComponentType.GROUP, LifetimeType.PERSISTENT, expiresAt, resetTimeoutOnUseDuration, emptyList(), oneUse, null, null, timeout, null, null) {
    override val group: ComponentGroupData? get() = null
}