package com.freya02.botcommands.api.commands.ratelimit

//TODO docs
interface CancellableRateLimit {
    val isRateLimitCancelled: Boolean

    fun cancelRateLimit()
}