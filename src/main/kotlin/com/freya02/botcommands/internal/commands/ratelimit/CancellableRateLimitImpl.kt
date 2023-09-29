package com.freya02.botcommands.internal.commands.ratelimit

import com.freya02.botcommands.api.commands.ratelimit.CancellableRateLimit

internal class CancellableRateLimitImpl internal constructor(): CancellableRateLimit {
    override var isRateLimitCancelled: Boolean = false

    override fun cancelRateLimit() {
        isRateLimitCancelled = true
    }
}