package io.github.freya022.botcommands.internal.components.ratelimit

import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitingContext
import io.github.freya022.botcommands.api.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketKeySupplier
import io.github.freya022.botcommands.internal.ratelimit.DefaultBucketKeySupplier

internal class DefaultBucketKeySupplierRequestHandler : DefaultBucketKeySupplier.RequestHandler {

    override fun handle(instance: DefaultBucketKeySupplier, context: RateLimitingContext): BucketKeySupplier.Key? {
        if (context is ComponentRateLimitingContext) {
            return instance.getRateLimitKey(context.event, context.rateLimitReference.toBucketKey())
        }

        return null
    }
}
