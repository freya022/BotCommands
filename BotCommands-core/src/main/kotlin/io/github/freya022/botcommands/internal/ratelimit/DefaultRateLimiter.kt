package io.github.freya022.botcommands.internal.ratelimit

import io.github.freya022.botcommands.api.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketAccessor
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketConfigurationSupplier
import io.github.freya022.botcommands.api.ratelimit.handler.RateLimitHandler

internal class DefaultRateLimiter internal constructor(
    private val scope: RateLimitScope,
    configurationSupplier: BucketConfigurationSupplier,
    private val deleteOnRefill: Boolean
) : RateLimiter,
    RateLimitHandler by RateLimitHandler.createDefault(scope, deleteOnRefill),
    BucketAccessor by BucketAccessor.createInMemory(scope, configurationSupplier) {

    override fun toString(): String {
        return "DefaultRateLimiter(scope=$scope)"
    }
}
