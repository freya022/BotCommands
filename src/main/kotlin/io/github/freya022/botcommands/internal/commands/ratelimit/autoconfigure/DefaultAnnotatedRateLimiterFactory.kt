package io.github.freya022.botcommands.internal.commands.ratelimit.autoconfigure

import io.github.freya022.botcommands.api.commands.ratelimit.AnnotatedRateLimiterFactory
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketConfigurationSupplier
import io.github.freya022.botcommands.internal.commands.ratelimit.DefaultRateLimiter

object DefaultAnnotatedRateLimiterFactory : AnnotatedRateLimiterFactory {

    override fun create(
        scope: RateLimitScope,
        configurationSupplier: BucketConfigurationSupplier,
        deleteOnRefill: Boolean,
    ): RateLimiter {
        return DefaultRateLimiter(scope, configurationSupplier, deleteOnRefill)
    }
}
