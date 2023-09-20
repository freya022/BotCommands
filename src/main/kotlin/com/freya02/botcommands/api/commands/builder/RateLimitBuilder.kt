package com.freya02.botcommands.api.commands.builder

import com.freya02.botcommands.api.commands.ratelimit.RateLimitHelperFactory
import com.freya02.botcommands.api.commands.ratelimit.RateLimitInfo
import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketFactory
import com.freya02.botcommands.internal.commands.CommandDSL

@CommandDSL
class RateLimitBuilder internal constructor(var bucketFactory: BucketFactory, var helperFactory: RateLimitHelperFactory) {
    internal fun build(): RateLimitInfo {
        return RateLimitInfo(helperFactory.get(bucketFactory))
    }
}