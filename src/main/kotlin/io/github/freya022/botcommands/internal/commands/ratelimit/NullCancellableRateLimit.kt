package io.github.freya022.botcommands.internal.commands.ratelimit

import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit

internal object NullCancellableRateLimit : CancellableRateLimit {
    override val isRateLimitCancelled: Boolean = false

    override fun cancelRateLimit() {}
}