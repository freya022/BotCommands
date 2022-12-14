package com.freya02.botcommands.internal.components.data

import kotlinx.datetime.Instant

class EphemeralTimeout(
    override val expirationTimestamp: Instant,
    val handler: (suspend () -> Unit)?
) : ComponentTimeout