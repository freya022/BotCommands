package com.freya02.botcommands.api.commands.ratelimit

import com.freya02.botcommands.api.commands.RateLimitScope
import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketAccessor
import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketFactory
import com.freya02.botcommands.api.commands.ratelimit.bucket.DefaultBucketAccessor
import com.freya02.botcommands.api.commands.ratelimit.handler.DefaultRateLimitHandler
import com.freya02.botcommands.api.commands.ratelimit.handler.RateLimitHandler

class DefaultRateLimitHelper(
    private val scope: RateLimitScope,
    private val bucketFactory: BucketFactory
) : RateLimitHelper,
    RateLimitHandler by DefaultRateLimitHandler(scope),
    BucketAccessor by DefaultBucketAccessor(scope, bucketFactory) {

    override fun toString(): String {
        return "DefaultRateLimitHelper(scope=$scope)"
    }
}