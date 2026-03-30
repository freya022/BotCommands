package io.github.freya022.botcommands.api.commands.ratelimit.bucket

import io.github.bucket4j.Bucket
import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.local.LocalBucket
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.internal.commands.ratelimit.DefaultBucketKeySupplier
import java.util.concurrent.ConcurrentHashMap

/**
 * Default in-memory [BucketAccessor] implementation using [RateLimitScope].
 *
 * **Note:** The rate limit scopes using guilds or channels are limited to guild-only events,
 * a user rate limit is applied if the limitation is violated.
 *
 * @param scope                 Scope of the rate limit, see [RateLimitScope] values.
 * @param configurationSupplier A supplier of [BucketConfiguration], describing the rate limits
 */
class InMemoryBucketAccessor(
    private val scope: RateLimitScope,
    private val configurationSupplier: BucketConfigurationSupplier
) : BucketAccessor {

    private val keySupplier = DefaultBucketKeySupplier(scope)
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
