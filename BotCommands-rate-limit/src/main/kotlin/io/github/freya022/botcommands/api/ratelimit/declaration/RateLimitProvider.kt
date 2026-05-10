package io.github.freya022.botcommands.api.ratelimit.declaration

import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.service.annotations.InterfacedService

/**
 * Interface to declare rate limits, ran once at startup.
 *
 * **Usage**: Register your instance as a service with [@BService][BService].
 *
 * ### Example
 * ```java
 * @Command
 * @NullMarked
 * public class SlashSkip implements RateLimitProvider {
 *     private static final String SKIP_RATE_LIMIT_NAME = "SlashSkip: skip";
 *
 *     @JDASlashCommand(name = "skip")
 *     @RateLimitReference(SKIP_RATE_LIMIT_NAME)
 *     public void onSlashSkip(GuildSlashEvent event) {
 *         // Handle command
 *     }
 *
 *     @Override
 *     public void declareRateLimit(RateLimitManager manager) {
 *         final var bucketFactory = Buckets.createSpikeProtected(
 *                 /* Capacity */ 5,
 *                 /* Duration */ Duration.ofMinutes(1),
 *                 /* Spike capacity */ 2,
 *                 /* Spike duration */ Duration.ofSeconds(5)
 *         );
 *         manager.rateLimit(
 *                 SKIP_RATE_LIMIT_NAME,
 *                 RateLimiter.createDefault(RateLimitScope.USER, BucketConfigurationSupplier.constant(bucketFactory), /* deleteOnRefill */ true)
 *         );
 *     }
 * }
 * ```
 *
 * @see RateLimitManager
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface RateLimitProvider {
    fun declareRateLimit(manager: RateLimitManager)
}
