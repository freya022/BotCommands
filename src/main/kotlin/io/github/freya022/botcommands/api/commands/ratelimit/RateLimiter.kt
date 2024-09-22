package io.github.freya022.botcommands.api.commands.ratelimit

import io.github.bucket4j.BucketConfiguration
import io.github.bucket4j.ConsumptionProbe
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter.Companion.createDefault
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter.Companion.createDefaultProxied
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.*
import io.github.freya022.botcommands.api.commands.ratelimit.handler.DefaultRateLimitHandler
import io.github.freya022.botcommands.api.commands.ratelimit.handler.RateLimitHandler
import io.github.freya022.botcommands.internal.commands.ratelimit.DefaultProxyRateLimiter
import io.github.freya022.botcommands.internal.commands.ratelimit.DefaultRateLimiter
import java.math.BigDecimal

/**
 * Retrieves rate limit buckets and handles rate limits by combining [BucketAccessor] and [RateLimitHandler].
 *
 * You can also make your own implementation by either implementing this interface directly
 * or by delegating both interfaces.
 *
 * ### Persistent bucket storage
 * Since [createDefault] stores buckets in-memory, the rate limits applied will be lost upon restart,
 * however you can use [createDefaultProxied], and then pass a [ProxyManager] which stores your buckets in persistent storage.
 *
 * @see createDefault
 * @see createDefaultProxied
 */
interface RateLimiter : BucketAccessor, RateLimitHandler {
    companion object {
        /**
         * Creates a default [RateLimiter] implementation,
         * see [DefaultRateLimitHandler] and [InMemoryBucketAccessor] for details.
         *
         * ### Example
         *
         * ```kt
         * @Command
         * class SlashInMemoryRateLimit : ApplicationCommand(), GlobalApplicationCommandProvider {
         *     fun onSlashInMemoryRateLimit(event: GuildSlashEvent) {
         *         event.reply_("Hi", ephemeral = true).queue()
         *     }
         *
         *     override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
         *         manager.slashCommand("in_memory_rate_limit", function = ::onSlashInMemoryRateLimit) {
         *             // Allow using the command once every 10 seconds
         *             // NOTE: this won't take effect if you are the bot owner
         *             val cooldown = Buckets.ofCooldown(10.seconds)
         *             // Apply limit on each user, regardless or guild/channel
         *             val rateLimiter = RateLimiter.createDefault(RateLimitScope.USER, cooldown.toSupplier())
         *             // Register anonymous rate limit, only on this command
         *             rateLimit(rateLimiter)
         *         }
         *     }
         * }
         * ```
         *
         * @param scope                 Scope of the rate limit, see [RateLimitScope] values.
         * @param configurationSupplier A supplier of [BucketConfiguration], describing the rate limits
         * @param deleteOnRefill        Whether the rate limit message should be deleted after the [refill delay][ConsumptionProbe.nanosToWaitForRefill].
         *
         * @see DefaultRateLimitHandler
         * @see InMemoryBucketAccessor
         *
         * @see RateLimitScope
         * @see Buckets
         */
        @JvmStatic
        fun createDefault(scope: RateLimitScope, configurationSupplier: BucketConfigurationSupplier, deleteOnRefill: Boolean = true): RateLimiter =
            DefaultRateLimiter(scope, configurationSupplier, deleteOnRefill)

        /**
         * Creates a [RateLimiter] implementation which retrieves its buckets using [proxyManager],
         * see [DefaultRateLimitHandler] and [ProxyBucketAccessor] for details.
         *
         * ### Example
         *
         * ```kt
         * @Configuration
         * @BService
         * open class ProxyManagerProvider {
         *     @Bean
         *     @BService
         *     open fun proxyManager(hikariSourceSupplier: HikariSourceSupplier): ProxyManager<BigDecimal> {
         *         // Create a proxy to manager buckets, persisting with PostgreSQL,
         *         // see https://bucket4j.com/8.14.0/toc.html#postgresqlselectforupdatebasedproxymanager
         *         return Bucket4jPostgreSQL.selectForUpdateBasedBuilder(hikariSourceSupplier.source)
         *             // Bucket expiration, needs to be triggered manually,
         *             // see https://bucket4j.com/8.14.0/toc.html#expiration-policy
         *             .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(1.minutes.toJavaDuration()))
         *             // RateLimiter#createDefaultProxied uses a BigDecimal key
         *             .primaryKeyMapper(PreparedStatement::setBigDecimal)
         *             .build()
         *     }
         * }
         * ```
         *
         * ```kt
         * @Command
         * class SlashPersistentRateLimit(private val proxyManager: ProxyManager<BigDecimal>) : ApplicationCommand(), GlobalApplicationCommandProvider {
         *     fun onSlashPersistentRateLimit(event: GuildSlashEvent) {
         *         event.reply_("Hi", ephemeral = true).queue()
         *     }
         *
         *     override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
         *         manager.slashCommand("persistent_rate_limit", function = ::onSlashPersistentRateLimit) {
         *             // Allow using the command once every hour
         *             // NOTE: this won't take effect if you are the bot owner
         *             val cooldown = Buckets.ofCooldown(1.hours)
         *             // Apply limit on each user, regardless or guild/channel
         *             val rateLimiter = RateLimiter.createDefaultProxied(RateLimitScope.USER, proxyManager, cooldown.toSupplier())
         *             // Register anonymous rate limit, only on this command
         *             rateLimit(rateLimiter)
         *         }
         *     }
         * }
         * ```
         *
         * @param scope                 Scope of the rate limit, see [RateLimitScope] values.
         * @param proxyManager          The proxy supplying buckets from a key, based on the [scope]
         * @param configurationSupplier A supplier of [BucketConfiguration], describing the rate limits
         * @param deleteOnRefill        Whether the rate limit message should be deleted after the [refill delay][ConsumptionProbe.nanosToWaitForRefill].
         *
         * @see DefaultRateLimitHandler
         * @see ProxyBucketAccessor
         *
         * @see RateLimitScope
         * @see Buckets
         */
        @JvmStatic
        fun createDefaultProxied(
            scope: RateLimitScope,
            proxyManager: ProxyManager<BigDecimal>,
            configurationSupplier: BucketConfigurationSupplier,
            deleteOnRefill: Boolean = true,
        ): RateLimiter =
            DefaultProxyRateLimiter(scope, proxyManager, configurationSupplier, deleteOnRefill)
    }
}