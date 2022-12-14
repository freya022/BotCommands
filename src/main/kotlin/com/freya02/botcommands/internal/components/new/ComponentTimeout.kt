package com.freya02.botcommands.internal.components.new

import kotlinx.datetime.Instant

interface ComponentTimeout {
    val expirationTimestamp: Instant
}