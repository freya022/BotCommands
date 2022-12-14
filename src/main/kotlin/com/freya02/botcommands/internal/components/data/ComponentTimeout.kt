package com.freya02.botcommands.internal.components.data

import kotlinx.datetime.Instant

interface ComponentTimeout {
    val expirationTimestamp: Instant
}