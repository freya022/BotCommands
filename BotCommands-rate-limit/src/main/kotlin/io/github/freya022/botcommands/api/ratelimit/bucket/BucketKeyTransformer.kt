package io.github.freya022.botcommands.api.ratelimit.bucket

/**
 * Transforms a bucket key into [K].
 * This is primarily used for [RateLimiter.createDefaultProxied][io.github.freya022.botcommands.api.ratelimit.RateLimiter.createDefaultProxied]
 */
fun interface BucketKeyTransformer<K> {
    fun transform(key: BucketKeySupplier.Key): K
}
