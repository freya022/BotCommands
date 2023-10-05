package io.github.freya022.botcommands.api.commands.ratelimit

import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter.Companion.defaultFactory
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketAccessor
import io.github.freya022.botcommands.api.commands.ratelimit.handler.RateLimitHandler
import io.github.freya022.botcommands.internal.commands.AbstractCommandInfo

/**
 * Retrieves rate limit buckets and handles rate limits by combining [BucketAccessor] and [RateLimitHandler].
 *
 * You can also make your own implementation by either implementing this interface directly,
 * or by delegating both interfaces.
 *
 * As this will be accessible in [AbstractCommandInfo.rateLimitInfo],
 * you can safe-cast into your instance and retrieve fields if you need to.
 *
 * @see DefaultRateLimiter
 * @see defaultFactory
 */
interface RateLimiter : BucketAccessor, RateLimitHandler {
    companion object {
        /**
         * @param scope          Scope of the rate limit, see [RateLimitScope] values.
         * @param deleteOnRefill Whether the rate limit message should be deleted after the [refill delay][ConsumptionProbe.nanosToWaitForRefill].
         *
         * @see RateLimitScope
         */
        @JvmStatic
        fun defaultFactory(scope: RateLimitScope, deleteOnRefill: Boolean = true): RateLimiterFactory = RateLimiterFactory { DefaultRateLimiter(scope, it, deleteOnRefill) }
    }
}