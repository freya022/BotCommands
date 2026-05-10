package io.github.freya022.botcommands.internal.ratelimit

import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.freya022.botcommands.api.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketAccessor
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketConfigurationSupplier
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketKeySupplier
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketKeySupplier.*
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketKeyTransformer
import io.github.freya022.botcommands.api.ratelimit.handler.RateLimitHandler

private object StringBucketKeyTransformer : BucketKeyTransformer<String> {
    override fun transform(key: Key): String {
        return when (key) {
            is PlaceKey -> "${key.identifier} ${key.id}"
            is UserKey -> "${key.identifier} ${key.id}"
            is UserAtPlaceKey -> "${key.identifier} ${key.placeId} ${key.userId}"
        }
    }
}

internal class DefaultProxyRateLimiter internal constructor(
    private val scope: RateLimitScope,
    proxyManager: ProxyManager<String>,
    bucketConfigurationSupplier: BucketConfigurationSupplier,
    private val deleteOnRefill: Boolean,
) : RateLimiter,
    BucketAccessor by BucketAccessor.createProxied(proxyManager, BucketKeySupplier.createDefault(scope), StringBucketKeyTransformer, bucketConfigurationSupplier),
    RateLimitHandler by RateLimitHandler.createDefault(scope, deleteOnRefill) {

    override fun toString(): String {
        return "DefaultProxyRateLimiter(scope=$scope, deleteOnRefill=$deleteOnRefill)"
    }
}
