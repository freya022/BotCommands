package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.commands.RateLimitScope
import com.freya02.botcommands.api.commands.annotations.*
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketFactory
import dev.minn.jda.ktx.messages.reply_
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@Command
class SlashRateLimit : ApplicationCommand() {
    @JDASlashCommand(name = "rate_limit_annotated")
    @RateLimit(
        scope = RateLimitScope.USER,
        baseBandwidth = Bandwidth(5, Refill(RefillType.GREEDY, 5, 1, ChronoUnit.MINUTES)),
        spikeBandwidth = Bandwidth(2, Refill(RefillType.INTERVAL, 2, 5, ChronoUnit.SECONDS))
    )
    fun onSlashRateLimit(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.slashCommand("rate_limit", function = ::onSlashRateLimit) {
            rateLimit(BucketFactory.spikeProtected(5, 1.minutes, 2, 5.seconds))
        }
    }
}