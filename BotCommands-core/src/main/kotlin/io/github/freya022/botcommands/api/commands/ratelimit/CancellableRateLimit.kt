package io.github.freya022.botcommands.api.commands.ratelimit

/**
 * Allows cancellation of rate limits by refilling tokens, usually delegated on events.
 */
interface CancellableRateLimit {
    val isRateLimitCancelled: Boolean

    /**
     * Cancels the token consumption of the current rate limited interaction.
     *
     * Canceling more than once is a no-op, canceling on a full bucket is also a no-op.
     */
    fun cancelRateLimit()
}