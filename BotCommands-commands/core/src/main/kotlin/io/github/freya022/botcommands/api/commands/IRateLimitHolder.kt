package io.github.freya022.botcommands.api.commands

/**
 * Holds a rate limiter.
 */
interface IRateLimitHolder {
    /**
     * Return `true` if this has a rate limiter.
     */
    fun hasRateLimiter(): Boolean
}