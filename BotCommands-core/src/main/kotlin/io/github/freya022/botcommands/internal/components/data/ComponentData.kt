package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.internal.components.ComponentType
import io.github.freya022.botcommands.internal.components.LifetimeType
import io.github.freya022.botcommands.internal.components.data.timeout.ComponentTimeout
import kotlin.time.Duration
import kotlin.time.Instant

internal sealed interface ComponentData {
    val internalId: Int
    val componentType: ComponentType
    val lifetimeType: LifetimeType
    val expiresAt: Instant?
    val timeout: ComponentTimeout?
    val resetTimeoutOnUseDuration: Duration?
}
