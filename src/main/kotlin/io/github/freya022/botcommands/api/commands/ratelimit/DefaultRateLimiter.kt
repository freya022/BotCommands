package io.github.freya022.botcommands.api.commands.ratelimit

import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketAccessor
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketFactory
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.DefaultBucketAccessor
import io.github.freya022.botcommands.api.commands.ratelimit.handler.DefaultRateLimitHandler
import io.github.freya022.botcommands.api.commands.ratelimit.handler.RateLimitHandler

/**
 * Default [RateLimiter] implementation, based on [DefaultRateLimitHandler] and [DefaultBucketAccessor].
 *
 * **Note:** The rate limit message won't be deleted in a private channel,
 * or if the [refill delay][ConsumptionProbe.nanosToWaitForRefill] is longer than 10 minutes.
 *
 * @param scope          Scope of the rate limit, see [RateLimitScope] values.
 * @param deleteOnRefill Whether the rate limit message should be deleted after the [refill delay][ConsumptionProbe.nanosToWaitForRefill].
 *
 * @see DefaultRateLimitHandler
 * @see DefaultBucketAccessor
 * @see RateLimitScope
 */
class DefaultRateLimiter(
    val scope: RateLimitScope,
    private val bucketFactory: BucketFactory,
    private val deleteOnRefill: Boolean = true
) : RateLimiter,
    RateLimitHandler by DefaultRateLimitHandler(scope, deleteOnRefill),
    BucketAccessor by DefaultBucketAccessor(scope, bucketFactory) {

    override fun toString(): String {
        return "DefaultRateLimiter(scope=$scope)"
    }
}