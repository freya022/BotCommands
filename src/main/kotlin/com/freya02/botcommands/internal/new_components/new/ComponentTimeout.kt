package com.freya02.botcommands.internal.new_components.new

import kotlinx.datetime.Instant

interface ComponentTimeout {
    val expirationTimestamp: Instant
}