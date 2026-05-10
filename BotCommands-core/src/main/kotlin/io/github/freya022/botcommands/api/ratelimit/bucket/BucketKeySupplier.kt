package io.github.freya022.botcommands.api.ratelimit.bucket

import io.github.freya022.botcommands.api.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.ratelimit.bucket.BucketKeySupplier.Companion.createDefault
import io.github.freya022.botcommands.internal.ratelimit.DefaultBucketKeySupplier

/**
 * Retrieves the bucket key given the execution context.
 *
 * You can use the provided parameters to create the key.
 *
 * @see createDefault
 */
interface BucketKeySupplier {
    fun getKey(context: RateLimitingContext): Key

    sealed interface Key {
        override fun equals(other: Any?): Boolean
        override fun hashCode(): Int
    }

    data class UserKey(val identifier: String, val id: Long): Key
    data class PlaceKey(val identifier: String, val id: Long): Key
    data class UserAtPlaceKey(val identifier: String, val placeId: Long, val userId: Long): Key

    companion object {
        /**
         * Creates a default [BucketKeySupplier] implementation,
         * creating bucket keys based on the interaction context and the configured [scope][RateLimitScope].
         *
         * @param scope The scope on which the rate limit should be applied to
         */
        @JvmStatic
        fun createDefault(scope: RateLimitScope): BucketKeySupplier {
            return DefaultBucketKeySupplier(scope)
        }
    }
}
