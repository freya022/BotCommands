package com.freya02.botcommands.api.commands.ratelimit

import com.freya02.botcommands.api.commands.RateLimitScope
import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketAccessor
import com.freya02.botcommands.api.commands.ratelimit.handler.RateLimitHandler

interface RateLimitHelper : BucketAccessor, RateLimitHandler {
    companion object {
        @JvmStatic
        fun defaultFactory(scope: RateLimitScope): RateLimitHelperFactory = RateLimitHelperFactory { DefaultRateLimitHelper(scope, it) }
    }
}