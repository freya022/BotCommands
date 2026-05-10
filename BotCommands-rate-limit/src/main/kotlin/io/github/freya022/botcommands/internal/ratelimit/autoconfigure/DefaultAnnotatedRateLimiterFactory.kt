package io.github.freya022.botcommands.internal.ratelimit.autoconfigure

import io.github.freya022.botcommands.api.ratelimit.AnnotatedRateLimiterFactory
import io.github.freya022.botcommands.api.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketConfigurationSupplier
import io.github.freya022.botcommands.internal.ratelimit.DefaultRateLimiter

object DefaultAnnotatedRateLimiterFactory : AnnotatedRateLimiterFactory {

    override fun create(
        scope: RateLimitScope,
        configurationSupplier: BucketConfigurationSupplier,
        deleteOnRefill: Boolean,
    ): RateLimiter {
        return DefaultRateLimiter(scope, configurationSupplier, deleteOnRefill)
    }
}
