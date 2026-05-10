package io.github.freya022.botcommands.internal.ratelimit

import io.github.freya022.botcommands.api.ratelimit.CancellableRateLimit

object NullCancellableRateLimit : CancellableRateLimit {
    override val isRateLimitCancelled: Boolean = false

    override fun cancelRateLimit() {}
}
