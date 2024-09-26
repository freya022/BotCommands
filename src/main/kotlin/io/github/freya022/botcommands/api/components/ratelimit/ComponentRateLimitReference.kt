package io.github.freya022.botcommands.api.components.ratelimit

import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitProvider
import io.github.freya022.botcommands.api.components.AbstractComponentFactory

/**
 * Represents a reference to a rate limiter previously declared by a [RateLimitProvider].
 *
 * Each rate limit reference is unique, the uniqueness can be qualified as a
 * composite primary key with both the [group] and [discriminator].
 *
 * @param group         The name of the rate limiter declared by a [RateLimitProvider]
 * @param discriminator Differentiates this component from others using the same [group], must be unique within the [group]
 *
 * @see AbstractComponentFactory.createRateLimitReference
 */
data class ComponentRateLimitReference internal constructor(val group: String, val discriminator: String) {
    /**
     * Transform this reference into a string that can be used as a bucket key.
     *
     * This assumes all references are unique, as otherwise some buckets could collide.
     */
    fun toBucketKey() = "$group $discriminator"
}