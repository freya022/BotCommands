package com.freya02.botcommands.api.commands.ratelimit

import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketFactory

fun interface RateLimitHelperFactory {
    fun get(bucketFactory: BucketFactory): RateLimitHelper
}