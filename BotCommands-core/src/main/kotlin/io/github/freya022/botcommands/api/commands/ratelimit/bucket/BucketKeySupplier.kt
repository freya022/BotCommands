package io.github.freya022.botcommands.api.commands.ratelimit.bucket

import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext

// TODO remove generic, let ProxyBucketAccessor accept a finisher to convert the Key object into a the primary key's type
/**
 * Retrieves the bucket key given the execution context.
 *
 * You can use the provided parameters to create the key.
 */
interface BucketKeySupplier<K> {
    fun getKey(context: RateLimitingContext): K
}
