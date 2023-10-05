package io.github.freya022.botcommands.internal.components.data

import io.github.freya022.botcommands.api.components.data.ComponentTimeout
import kotlinx.datetime.Instant

internal class EphemeralTimeout(
    override val expirationTimestamp: Instant,
    val handler: (suspend () -> Unit)?
) : ComponentTimeout