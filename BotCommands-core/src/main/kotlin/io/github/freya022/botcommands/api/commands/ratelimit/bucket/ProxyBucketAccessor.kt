package io.github.freya022.botcommands.api.commands.ratelimit.bucket

import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext

/**
 * [BucketAccessor] implementation with a [ProxyManager] to retrieve buckets,
 * and a key made using [keySupplier].
 *
 * @param proxyManager          Scope of the rate limit, see [RateLimitScope] values.
 * @param keySupplier           Supplies the key to create/retrieve a bucket using the [proxyManager]
 * @param keyTransformer        Transforms the bucket key into the proxy's expected key type
 * @param configurationSupplier A supplier of [BucketConfiguration], describing the rate limits
 */
class ProxyBucketAccessor<K>(
    private val proxyManager: ProxyManager<K>,
    private val keySupplier: BucketKeySupplier,
    private val keyTransformer: BucketKeyTransformer<K>,
    private val configurationSupplier: BucketConfigurationSupplier,
) : BucketAccessor {

    override suspend fun getBucket(context: RateLimitingContext): Bucket {
        val proxyKey = keyTransformer.transform(keySupplier.getKey(context))
        return proxyManager.getProxy(proxyKey) { configurationSupplier.getConfiguration(context) }
    }
}
