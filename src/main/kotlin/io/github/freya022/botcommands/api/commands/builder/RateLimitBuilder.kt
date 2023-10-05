package io.github.freya022.botcommands.api.commands.builder

import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiterFactory
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketFactory
import io.github.freya022.botcommands.internal.commands.CommandDSL

@CommandDSL
class RateLimitBuilder internal constructor(var bucketFactory: BucketFactory, var limiterFactory: RateLimiterFactory) {
    internal fun build(): RateLimitInfo {
        return RateLimitInfo(limiterFactory.get(bucketFactory))
    }
}