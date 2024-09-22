package doc.kotlin.examples.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.Buckets
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.toSupplier
import kotlin.time.Duration.Companion.seconds

@Command
class SlashInMemoryRateLimit : ApplicationCommand(), GlobalApplicationCommandProvider {
    fun onSlashInMemoryRateLimit(event: GuildSlashEvent) {
        event.reply_("Hi", ephemeral = true).queue()
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("in_memory_rate_limit", function = ::onSlashInMemoryRateLimit) {
            // Allow using the command once every 10 seconds
            // NOTE: this won't take effect if you are the bot owner
            val cooldown = Buckets.ofCooldown(10.seconds)
            // Apply limit on each user, regardless or guild/channel
            val rateLimiter = RateLimiter.createDefault(RateLimitScope.USER, cooldown.toSupplier())
            // Register anonymous rate limit, only on this command
            rateLimit(rateLimiter)
        }
    }
}