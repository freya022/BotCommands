package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.messages.into
import dev.minn.jda.ktx.messages.reply_
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy
import io.github.bucket4j.postgresql.Bucket4jPostgreSQL
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.RateLimitReference
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketConfigurationSupplier
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.Buckets
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitManager
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitProvider
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.core.db.HikariSourceSupplier
import java.sql.PreparedStatement
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private const val commandRateLimitGroup = "SlashRateLimit: my_rate_limit"
private const val retryRateLimitGroup = "SlashRateLimit: my_retry_rate_limit"

@Command
@RequiresComponents
class SlashRateLimit(
    private val buttons: Buttons,
    hikariSourceSupplier: HikariSourceSupplier
) : ApplicationCommand(), GlobalApplicationCommandProvider, RateLimitProvider {
    private val proxyManager = Bucket4jPostgreSQL.selectForUpdateBasedBuilder(hikariSourceSupplier.source)
        .expirationAfterWrite(ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(1.minutes.toJavaDuration()))
        .primaryKeyMapper(PreparedStatement::setBigDecimal)
        .build()

    @JDASlashCommand(name = "rate_limit_annotated")
//    @RateLimit(
//        scope = RateLimitScope.USER,
//        baseBandwidth = Bandwidth(5, Refill(RefillType.GREEDY, 5, 1, ChronoUnit.MINUTES)),
//        spikeBandwidth = Bandwidth(2, Refill(RefillType.INTERVAL, 2, 5, ChronoUnit.SECONDS))
//    )
    @RateLimitReference(commandRateLimitGroup)
    suspend fun onSlashRateLimit(event: GuildSlashEvent) {
        val button = buttons.primary("Retry (5 clicks in 1 minute)").ephemeral {
            rateLimitReference(retryRateLimitGroup)
            bindTo { event ->
                if (Math.random() > 0.5) {
                    event.cancelRateLimit()
                    event.reply_("token added back, try again", ephemeral = true).queue()
                } else {
                    event.reply_("ok", ephemeral = true).queue()
                }
            }
        }
        event.reply_(components = button.into(), ephemeral = true).queue()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("rate_limit", function = ::onSlashRateLimit) {
//            rateLimit(bucketFactory)
            rateLimitReference(commandRateLimitGroup)
        }
    }

    override fun declareRateLimit(manager: RateLimitManager) {
        val commandBucketConfiguration = Buckets.ofCooldown(1.hours)
        val commandRateLimiter = RateLimiter.createDefault(
            RateLimitScope.USER,
            configurationSupplier = BucketConfigurationSupplier.constant(commandBucketConfiguration),
            deleteOnRefill = true
        )

        val retryBucketConfiguration = Buckets.spikeProtected(5, 1.hours, 1, 5.seconds)
        val retryRateLimiter = RateLimiter.createDefaultProxied(
            RateLimitScope.USER,
            proxyManager,
            configurationSupplier = BucketConfigurationSupplier.constant(retryBucketConfiguration),
            deleteOnRefill = true
        )

        manager.rateLimit(commandRateLimitGroup, commandRateLimiter)
        manager.rateLimit(retryRateLimitGroup, retryRateLimiter)
    }
}