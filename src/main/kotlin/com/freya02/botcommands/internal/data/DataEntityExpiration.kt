package com.freya02.botcommands.internal.data

import kotlinx.datetime.Instant

data class DataEntityExpiration(val expirationInstant: Instant, val handlerName: String?)