package io.github.freya022.botcommands.internal.commands.ratelimit

import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketAccessor
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketConfigurationSupplier
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.InMemoryBucketAccessor
import io.github.freya022.botcommands.api.commands.ratelimit.handler.DefaultRateLimitHandler
import io.github.freya022.botcommands.api.commands.ratelimit.handler.RateLimitHandler

internal class DefaultRateLimiter internal constructor(
    private val scope: RateLimitScope,
    configurationSupplier: BucketConfigurationSupplier,
    private val deleteOnRefill: Boolean
) : RateLimiter,
    RateLimitHandler by DefaultRateLimitHandler(scope, deleteOnRefill),
    BucketAccessor by InMemoryBucketAccessor(scope, configurationSupplier) {

    override fun toString(): String {
        return "DefaultRateLimiter(scope=$scope)"
    }
}