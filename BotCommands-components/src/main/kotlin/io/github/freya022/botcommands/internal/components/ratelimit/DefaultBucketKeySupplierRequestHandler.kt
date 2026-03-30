package io.github.freya022.botcommands.internal.components.ratelimit

import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketKeySupplier
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitingContext
import io.github.freya022.botcommands.internal.commands.ratelimit.DefaultBucketKeySupplier

internal class DefaultBucketKeySupplierRequestHandler : DefaultBucketKeySupplier.RequestHandler {

    override fun handle(instance: DefaultBucketKeySupplier, context: RateLimitingContext): BucketKeySupplier.Key? {
        if (context is ComponentRateLimitingContext) {
            return instance.getRateLimitKey(context.event, context.rateLimitReference.toBucketKey())
        }

        return null
    }
}
