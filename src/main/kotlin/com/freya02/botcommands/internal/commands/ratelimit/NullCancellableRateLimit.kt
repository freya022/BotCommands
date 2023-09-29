package com.freya02.botcommands.internal.commands.ratelimit

import com.freya02.botcommands.api.commands.ratelimit.CancellableRateLimit

internal object NullCancellableRateLimit : CancellableRateLimit {
    override val isRateLimitCancelled: Boolean = false

    override fun cancelRateLimit() {}
}