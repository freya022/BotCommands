package io.github.freya022.botcommands.api.ratelimit.bucket

import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.freya022.botcommands.api.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketAccessor.Companion.createInMemory
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketAccessor.Companion.createProxied
import io.github.freya022.botcommands.internal.ratelimit.bucket.InMemoryBucketAccessor
import io.github.freya022.botcommands.internal.ratelimit.bucket.ProxyBucketAccessor

/**
 * Retrieves rate limit buckets given the execution context.
 *
 * @see createInMemory
 * @see createProxied
 */
interface BucketAccessor {
    suspend fun getBucket(context: RateLimitingContext): Bucket

    companion object {
        /**
         * Creates an in-memory [BucketAccessor] implementation using [RateLimitScope].
         *
         * **Note:** The rate limit scopes using guilds or channels are limited to guild-only events,
         * a user rate limit is applied if the limitation is violated.
         *
         * @param scope                 Scope of the rate limit, see [RateLimitScope] values.
         * @param configurationSupplier A supplier of [BucketConfiguration], describing the rate limits
         */
        @JvmStatic
        fun createInMemory(scope: RateLimitScope, configurationSupplier: BucketConfigurationSupplier): BucketAccessor {
            return InMemoryBucketAccessor(scope, configurationSupplier)
        }

        /**
         * Creates a [BucketAccessor] implementation with a [ProxyManager] to retrieve buckets,
         * and a key made using [keySupplier].
         *
         * @param proxyManager          Scope of the rate limit, see [RateLimitScope] values.
         * @param keySupplier           Supplies the key to create/retrieve a bucket using the [proxyManager]
         * @param keyTransformer        Transforms the bucket key into the proxy's expected key type
         * @param configurationSupplier A supplier of [BucketConfiguration], describing the rate limits
         */
        @JvmStatic
        fun <K> createProxied(
            proxyManager: ProxyManager<K>,
            keySupplier: BucketKeySupplier,
            keyTransformer: BucketKeyTransformer<K>,
            configurationSupplier: BucketConfigurationSupplier,
        ): BucketAccessor {
            return ProxyBucketAccessor(proxyManager, keySupplier, keyTransformer, configurationSupplier)
        }
    }
}
