package io.github.freya022.botcommands.api.commands.ratelimit.declaration

import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
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
 *     public void declareRateLimit(@NotNull RateLimitManager manager) {
 *         final var bucketFactory = BucketFactory.spikeProtected(
 *                 /* Capacity */ 5,
 *                 /* Duration */ Duration.ofMinutes(1),
 *                 /* Spike capacity */ 2,
 *                 /* Spike duration */ Duration.ofSeconds(5)
 *         );
 *         manager.rateLimit(SKIP_RATE_LIMIT_NAME, bucketFactory);
 *     }
 * }
 * ```
 *
 * @see CommandBuilder.rateLimit In-command equivalent
 * @see RateLimitManager
 * @see InterfacedService @InterfacedService
 */
@InterfacedService(acceptMultiple = true)
interface RateLimitProvider {
    fun declareRateLimit(manager: RateLimitManager)
}