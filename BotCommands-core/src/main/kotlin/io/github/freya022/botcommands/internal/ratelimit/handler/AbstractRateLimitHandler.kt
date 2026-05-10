package io.github.freya022.botcommands.internal.ratelimit.handler

import io.github.bucket4j.Bucket
import io.github.freya022.botcommands.api.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.api.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.internal.ratelimit.CancellableRateLimitImpl

abstract class AbstractRateLimitHandler protected constructor() {
    protected suspend fun tryRun(rateLimitingContext: RateLimitingContext, rateLimitInfo: RateLimitInfo, block: suspend (CancellableRateLimit) -> Boolean) {
        val bucket = rateLimitInfo.limiter.getBucket(rateLimitingContext)
        val probe = bucket.tryConsumeAndReturnRemaining(1)
        if (probe.isConsumed) {
            runRateLimited(block, bucket)
        } else {
            rateLimitInfo.limiter.onRateLimit(rateLimitingContext, probe)
        }
    }

    private suspend fun runRateLimited(block: suspend (CancellableRateLimit) -> Boolean, bucket: Bucket) {
        val cancellableRateLimit = CancellableRateLimitImpl(bucket)
        try {
            if (!block(cancellableRateLimit)) {
                cancellableRateLimit.cancelRateLimit()
            }
        } catch (e: Throwable) {
            cancellableRateLimit.cancelRateLimit()
            throw e
        }
    }
}
