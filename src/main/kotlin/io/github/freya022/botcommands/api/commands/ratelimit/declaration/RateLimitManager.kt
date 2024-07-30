@file:IgnoreStackFrame // Due to extensions

package io.github.freya022.botcommands.api.commands.ratelimit.declaration

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.commands.annotations.Cooldown
import io.github.freya022.botcommands.api.commands.annotations.RateLimitReference
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.declaration.AutocompleteHandlerProvider
import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.commands.builder.RateLimitBuilder
import io.github.freya022.botcommands.api.commands.ratelimit.*
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketFactory
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.annotations.IgnoreStackFrame
import io.github.freya022.botcommands.internal.commands.builder.RateLimitBuilderImpl
import kotlin.time.Duration
import java.time.Duration as JavaDuration

/**
 * Allows programmatic declaration of autocomplete handlers using [AutocompleteHandlerProvider].
 *
 * @see AutocompleteHandlerProvider
 */
@IgnoreStackFrame // Due to the abstract method
abstract class RateLimitManager internal constructor() {
    abstract val context: BContext

    internal abstract fun createRateLimit(
        group: String,
        bucketFactory: BucketFactory,
        limiterFactory: RateLimiterFactory,
        block: RateLimitBuilderImpl.() -> Unit
    ): RateLimitInfo

    /**
     * Creates a rate limiter with the specified group.
     *
     * The created rate limiter can be used in [CommandBuilder.rateLimitReference] and [@RateLimitReference][RateLimitReference].
     *
     * @param group          The name of the rate limiter
     * @param bucketFactory  The bucket factory to use in [RateLimiterFactory]
     * @param limiterFactory The [RateLimiter] factory in charge of handling buckets and rate limits
     * @param block          Further configures the [RateLimitBuilder]
     *
     * @throws IllegalStateException If a rate limiter with the same group exists
     */
    @JvmOverloads
    fun rateLimit(
        group: String,
        bucketFactory: BucketFactory,
        limiterFactory: RateLimiterFactory = RateLimiter.defaultFactory(RateLimitScope.USER),
        block: ReceiverConsumer<RateLimitBuilder> = ReceiverConsumer.noop()
    ): RateLimitInfo {
        return createRateLimit(group, bucketFactory, limiterFactory, block)
    }

    /**
     * Creates a rate limit-based cooldown.
     *
     * ### Cooldown cancellation
     * The cooldown can be cancelled inside the command with [CancellableRateLimit.cancelRateLimit] on your event.
     *
     * @param group    The name of the underlying rate limiter
     * @param scope    The scope of the cooldown
     * @param duration The duration before the cooldown expires
     * @param block    Further configures the [RateLimitBuilder]
     *
     * @see Cooldown @Cooldown
     * @see rateLimit
     */
    @JvmOverloads
    fun cooldown(
        group: String,
        duration: JavaDuration,
        scope: RateLimitScope = RateLimitScope.USER,
        deleteOnRefill: Boolean = true,
        block: ReceiverConsumer<RateLimitBuilder> = ReceiverConsumer.noop()
    ): RateLimitInfo {
        return rateLimit(group, BucketFactory.ofCooldown(duration), RateLimiter.defaultFactory(scope, deleteOnRefill), block)
    }
}

/**
 * Creates a rate limit-based cooldown.
 *
 * ### Cooldown cancellation
 * The cooldown can be cancelled inside the command with [CancellableRateLimit.cancelRateLimit] on your event.
 *
 * @param group          The name of the underlying rate limiter
 * @param scope          The scope of the cooldown
 * @param duration       The duration before the cooldown expires
 * @param deleteOnRefill Whether the cooldown messages should be deleted after the cooldown expires
 * @param block          Further configures the [RateLimitBuilder]
 *
 * @see Cooldown @Cooldown
 * @see RateLimitManager.rateLimit
 */
fun RateLimitManager.cooldown(
    group: String,
    duration: Duration,
    scope: RateLimitScope = RateLimitScope.USER,
    deleteOnRefill: Boolean = true,
    block: ReceiverConsumer<RateLimitBuilder> = ReceiverConsumer.noop()
): RateLimitInfo {
    return rateLimit(group, BucketFactory.ofCooldown(duration), RateLimiter.defaultFactory(scope, deleteOnRefill), block)
}