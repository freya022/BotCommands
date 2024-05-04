package io.github.freya022.botcommands.api.commands.annotations

import io.github.bucket4j.Bucket
import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitManager
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitProvider
import java.time.temporal.ChronoUnit

/**
 * Refill type of bandwidths.
 *
 * @see Refill @Refill
 * @see Bandwidth @Bandwidth
 * @see io.github.bucket4j.Refill Bucket4J Refill
 */
enum class RefillType {
    /**
     * Refill which will try to add the tokens to the bucket as soon as possible.
     *
     * For example, "10 tokens per 1 second" will add 1 token per each 100 milliseconds,
     * in other words, it will not wait 1 second to regenerate 10 tokens.
     */
    GREEDY,

    /**
     * Refill on every interval of time,
     * "10 tokens per 1 second" will exactly regenerate 10 tokens every second.
     */
    INTERVAL
}

/**
 * Represents one of the limits of a rate limit bucket.
 *
 * All bandwidths need to have a token available when requesting a token from the bucket.
 *
 * @see io.github.bucket4j.Bandwidth Bucket4J Bandwidth
 */
@Target
@Retention(AnnotationRetention.RUNTIME)
annotation class Bandwidth(
    val capacity: Long,
    val refill: Refill
)

/**
 * Defines how the tokens are refilled on each [bandwidth][Bandwidth].
 *
 * @see io.github.bucket4j.Refill Bucket4J Refill
 */
@Target
@Retention(AnnotationRetention.RUNTIME)
annotation class Refill(
    val type: RefillType,
    /** The number of tokens added to the bandwidth */
    val tokens: Long,
    /**
     * The period over which tokens will be added progressively (greedy),
     * or which tokens will be added at once (intervally)
     */
    val period: Long,
    /** The unit of [period] */
    val periodUnit: ChronoUnit
)

/**
 * Defines a rate limit for a command / component handler.
 *
 * **Text commands note:** This applies to the command itself, not only this variation,
 * in other words, this applies to all commands with the same path.
 *
 * ### Rate limit cancellation
 * The rate limit can be cancelled inside the command with [CancellableRateLimit.cancelRateLimit] on your event.
 *
 * ### Example
 * ```java
 * @RateLimit(
 *     scope = RateLimitScope.USER, bandwidths = {
 *     @Bandwidth(capacity = 5, refill = @Refill(type = RefillType.GREEDY, tokens = 5, period = 1, periodUnit = ChronoUnit.MINUTES)),
 *     @Bandwidth(capacity = 2, refill = @Refill(type = RefillType.INTERVAL, tokens = 2, period = 5, periodUnit = ChronoUnit.SECONDS))
 * })
 * @JDASlashCommand(...)
 * public void onSlashRateLimit(...) { ... }
 * ```
 *
 * @see RateLimitScope
 * @see Bandwidth @Bandwidth
 * @see Bucket
 *
 * @see RateLimitManager.rateLimit DSL equivalent
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimit(
    /**
     * Scope of the rate limit, see [RateLimitScope] values.
     *
     * @see RateLimitScope
     */
    val scope: RateLimitScope,
    /**
     * Whether the rate limit message should be deleted after the rate limit has expired.
     *
     * **Note:** The rate limit message won't be deleted in a private channel,
     * or if the [refill delay][ConsumptionProbe.nanosToWaitForRefill] is longer than 10 minutes.
     *
     * **Default:** `true`
     */
    val deleteOnRefill: Boolean = true,
    vararg val bandwidths: Bandwidth
)

/**
 * Uses an existing rate limiter for this command / component handler.
 *
 * **Text commands note:** This applies to the command itself, not only this variation,
 * in other words, this applies to all commands with the same path.
 *
 * See [RateLimitProvider] for examples.
 *
 * @see RateLimitProvider
 * @see CommandBuilder.rateLimitReference DSL equivalent
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimitReference(@get:JvmName("value") val group: String)