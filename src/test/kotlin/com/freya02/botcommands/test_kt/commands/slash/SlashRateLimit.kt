package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.annotations.RateLimitReference
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.ratelimit.RateLimitContainer
import com.freya02.botcommands.api.commands.ratelimit.annotations.RateLimitDeclaration
import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketFactory
import dev.minn.jda.ktx.messages.reply_
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val bucketFactory = BucketFactory.spikeProtected(5, 1.minutes, 2, 5.seconds)

private const val rateLimitGroup = "SlashRateLimit: my_rate_limit"

@Command
class SlashRateLimit : ApplicationCommand() {
    @JDASlashCommand(name = "rate_limit_annotated")
//    @RateLimit(
//        scope = RateLimitScope.USER,
//        baseBandwidth = Bandwidth(5, Refill(RefillType.GREEDY, 5, 1, ChronoUnit.MINUTES)),
//        spikeBandwidth = Bandwidth(2, Refill(RefillType.INTERVAL, 2, 5, ChronoUnit.SECONDS))
//    )
    @RateLimitReference(rateLimitGroup)
    fun onSlashRateLimit(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.slashCommand("rate_limit", function = ::onSlashRateLimit) {
//            rateLimit(bucketFactory)
            rateLimitReference(rateLimitGroup)
        }
    }

    @RateLimitDeclaration
    fun declare(rateLimitContainer: RateLimitContainer) {
        rateLimitContainer.rateLimit(rateLimitGroup, bucketFactory)
    }
}