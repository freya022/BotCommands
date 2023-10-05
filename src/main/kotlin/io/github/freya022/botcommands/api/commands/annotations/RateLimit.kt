package io.github.freya022.botcommands.api.commands.annotations

import io.github.bucket4j.Bucket
import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitContainer
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import java.time.temporal.ChronoUnit

/**
 * Refill type of bandwidths.
 *
 * @see Refill @Refill
 * @see Bandwidth @Bandwidth
 * @see io.github.bucket4j.Refill Bucket4J Refill
 */
enum class RefillType {
    GREEDY,
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
 * ### Example (Kotlin)
 * ```kt
 * @RateLimit(
 *     scope = RateLimitScope.USER,
 *     Bandwidth(5, Refill(RefillType.GREEDY, 5, 1, ChronoUnit.MINUTES)),
 *     Bandwidth(2, Refill(RefillType.INTERVAL, 2, 5, ChronoUnit.SECONDS))
 * )
 * @JDASlashCommands(...)
 * fun onSlashRateLimit(...) { ... }
 * ```
 *
 * ### Example (Java)
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
 * ### Example
 * ```kt
 * @RateLimitDeclaration
 * fun declare(rateLimitContainer: RateLimitContainer) {
 *     val bucketFactory = BucketFactory.spikeProtected(5, 1.minutes, 2, 5.seconds)
 *     rateLimitContainer.rateLimit("SlashMyCommand: my_rate_limit", bucketFactory)
 * }
 *
 * @RateLimitReference("SlashMyCommand: my_rate_limit")
 * @JDASlashCommand(...)
 * fun onSlashMyCommand(...) { ... }
 * ```
 *
 * ### Example (Java)
 * ```java
 * @RateLimitDeclaration
 * public void declare(RateLimitContainer rateLimitContainer) {
 *     var bucketFactory = BucketFactory.spikeProtected(5, Duration.ofMinutes(1), 2, Duration.ofSeconds(5))
 *     rateLimitContainer.rateLimit("SlashMyCommand: my_rate_limit", bucketFactory)
 * }
 *
 * @RateLimitReference(group = "SlashMyCommand: my_rate_limit")
 * @JDASlashCommand(...)
 * public void onSlashMyCommand(...) { ... }
 * ```
 *
 * @see RateLimitContainer.rateLimit
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimitReference(val group: String)