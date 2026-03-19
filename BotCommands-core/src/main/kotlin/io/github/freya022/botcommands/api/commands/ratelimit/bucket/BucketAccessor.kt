package io.github.freya022.botcommands.api.commands.ratelimit.bucket

import io.github.bucket4j.Bucket
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext

/**
 * Retrieves rate limit buckets given the execution context.
 *
 * @see InMemoryBucketAccessor
 * @see ProxyBucketAccessor
 */
interface BucketAccessor {
    suspend fun getBucket(context: RateLimitingContext): Bucket
}
