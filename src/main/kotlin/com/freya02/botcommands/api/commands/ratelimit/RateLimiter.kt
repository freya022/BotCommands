package com.freya02.botcommands.api.commands.ratelimit

import com.freya02.botcommands.api.commands.RateLimitScope
import com.freya02.botcommands.api.commands.ratelimit.RateLimiter.Companion.defaultFactory
import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketAccessor
import com.freya02.botcommands.api.commands.ratelimit.handler.RateLimitHandler
import com.freya02.botcommands.internal.commands.AbstractCommandInfo

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
        @JvmStatic
        fun defaultFactory(scope: RateLimitScope): RateLimiterFactory = RateLimiterFactory { DefaultRateLimiter(scope, it) }
    }
}