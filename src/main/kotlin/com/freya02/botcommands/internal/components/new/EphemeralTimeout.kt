package com.freya02.botcommands.internal.components.new

import kotlinx.datetime.Instant

class EphemeralTimeout(
    override val expirationTimestamp: Instant,
    val handler: (suspend () -> Unit)?
) : ComponentTimeout