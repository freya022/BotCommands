package io.github.freya022.botcommands.internal.commands.ratelimit.bucket

import io.github.bucket4j.Bucket
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketAccessor
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketConfigurationSupplier
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketKeySupplier
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketKeyTransformer

internal class ProxyBucketAccessor<K> internal constructor(
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
