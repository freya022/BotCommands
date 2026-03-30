package io.github.freya022.botcommands.api.commands.ratelimit.bucket

import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext

/**
 * Retrieves the bucket key given the execution context.
 *
 * You can use the provided parameters to create the key.
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
}
