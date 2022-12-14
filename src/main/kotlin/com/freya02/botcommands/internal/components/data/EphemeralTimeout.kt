package com.freya02.botcommands.internal.components.data

import com.freya02.botcommands.api.components.data.ComponentTimeout
import kotlinx.datetime.Instant

internal class EphemeralTimeout(
    override val expirationTimestamp: Instant,
    val handler: (suspend () -> Unit)?
) : ComponentTimeout