package com.freya02.botcommands.api.commands.ratelimit

import com.freya02.botcommands.api.commands.RateLimitScope
import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketAccessor
import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketFactory
import com.freya02.botcommands.api.commands.ratelimit.bucket.DefaultBucketAccessor
import com.freya02.botcommands.api.commands.ratelimit.handler.DefaultRateLimitHandler
import com.freya02.botcommands.api.commands.ratelimit.handler.RateLimitHandler

/**
 * Default [RateLimiter] implementation, based on [DefaultRateLimitHandler] and [DefaultBucketAccessor].
 */
class DefaultRateLimiter(
    private val scope: RateLimitScope,
    private val bucketFactory: BucketFactory
) : RateLimiter,
    RateLimitHandler by DefaultRateLimitHandler(scope),
    BucketAccessor by DefaultBucketAccessor(scope, bucketFactory) {

    override fun toString(): String {
        return "DefaultRateLimiter(scope=$scope)"
    }
}