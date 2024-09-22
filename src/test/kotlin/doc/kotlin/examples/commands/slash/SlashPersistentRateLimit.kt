package doc.kotlin.examples.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.bucket4j.distributed.proxy.ProxyManager
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.Buckets
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.toSupplier
import java.math.BigDecimal
import kotlin.time.Duration.Companion.hours

@Command
class SlashPersistentRateLimit(private val proxyManager: ProxyManager<BigDecimal>) : ApplicationCommand(), GlobalApplicationCommandProvider {
    fun onSlashPersistentRateLimit(event: GuildSlashEvent) {
        event.reply_("Hi", ephemeral = true).queue()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("persistent_rate_limit", function = ::onSlashPersistentRateLimit) {
            // Allow using the command once every hour
            // NOTE: this won't take effect if you are the bot owner
            val cooldown = Buckets.ofCooldown(1.hours)
            // Apply limit on each user, regardless or guild/channel
            val rateLimiter = RateLimiter.createDefaultProxied(RateLimitScope.USER, proxyManager, cooldown.toSupplier())
            // Register anonymous rate limit, only on this command
            rateLimit(rateLimiter)
        }
    }
}