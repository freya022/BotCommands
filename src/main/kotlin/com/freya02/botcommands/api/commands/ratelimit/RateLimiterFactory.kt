package com.freya02.botcommands.api.commands.ratelimit

import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketFactory

fun interface RateLimiterFactory {
    fun get(bucketFactory: BucketFactory): RateLimiter
}