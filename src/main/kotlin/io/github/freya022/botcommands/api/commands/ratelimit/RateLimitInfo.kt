package io.github.freya022.botcommands.api.commands.ratelimit

/**
 * Container for rate limiters data.
 */
class RateLimitInfo internal constructor(val group: String, val limiter: RateLimiter) {
    override fun toString(): String {
        return "RateLimitInfo(group='$group', limiter=$limiter)"
    }
}