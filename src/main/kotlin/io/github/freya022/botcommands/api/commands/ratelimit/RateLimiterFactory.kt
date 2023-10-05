package io.github.freya022.botcommands.api.commands.ratelimit

import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketFactory

fun interface RateLimiterFactory {
    fun get(bucketFactory: BucketFactory): RateLimiter
}