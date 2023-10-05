package io.github.freya022.botcommands.api.components.data

import kotlinx.datetime.Instant

interface ComponentTimeout {
    val expirationTimestamp: Instant
}