package com.freya02.botcommands.api.components.data

import kotlinx.datetime.Instant

interface ComponentTimeout {
    val expirationTimestamp: Instant
}