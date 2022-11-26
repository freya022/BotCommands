package com.freya02.botcommands.internal.data

import kotlinx.datetime.Instant

@Deprecated("To be removed")
data class DataEntityExpiration(val expirationInstant: Instant, val handlerName: String?)