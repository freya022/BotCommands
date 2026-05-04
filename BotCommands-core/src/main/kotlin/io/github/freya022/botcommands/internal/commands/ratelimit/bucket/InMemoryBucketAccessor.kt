package io.github.freya022.botcommands.internal.commands.ratelimit.bucket

import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.local.LocalBucket
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketAccessor
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketConfigurationSupplier
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketKeySupplier
import java.util.concurrent.ConcurrentHashMap

internal class InMemoryBucketAccessor internal constructor(
    scope: RateLimitScope,
    private val configurationSupplier: BucketConfigurationSupplier
) : BucketAccessor {

    private val keySupplier = BucketKeySupplier.createDefault(scope)
    private val map: MutableMap<BucketKeySupplier.Key, Bucket> = ConcurrentHashMap()

    override suspend fun getBucket(context: RateLimitingContext): Bucket {
        val key = keySupplier.getKey(context)
        return map.computeIfAbsent(key) {
            configurationSupplier.getConfiguration(context).toBucket()
        }
    }

    private fun BucketConfiguration.toBucket(): LocalBucket {
        return Bucket.builder()
            .apply { bandwidths.forEach(::addLimit) }
            .build()
    }
}
