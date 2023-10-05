package io.github.freya022.botcommands.test_kt.commands.slash

import dev.minn.jda.ktx.messages.into
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.RateLimitReference
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitContainer
import io.github.freya022.botcommands.api.commands.ratelimit.annotations.RateLimitDeclaration
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketFactory
import io.github.freya022.botcommands.api.components.Components
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

private val commandBucketFactory = BucketFactory.spikeProtected(5, 1.minutes, 2, 5.seconds)
private val retryBucketFactory = BucketFactory.spikeProtected(5, 1.hours, 1, 5.seconds)

private const val commandRateLimitGroup = "SlashRateLimit: my_rate_limit"
private const val retryRateLimitGroup = "SlashRateLimit: my_retry_rate_limit"

@Command
class SlashRateLimit(private val components: Components) : ApplicationCommand() {
    @JDASlashCommand(name = "rate_limit_annotated")
//    @RateLimit(
//        scope = RateLimitScope.USER,
//        baseBandwidth = Bandwidth(5, Refill(RefillType.GREEDY, 5, 1, ChronoUnit.MINUTES)),
//        spikeBandwidth = Bandwidth(2, Refill(RefillType.INTERVAL, 2, 5, ChronoUnit.SECONDS))
//    )
    @RateLimitReference(commandRateLimitGroup)
    fun onSlashRateLimit(event: GuildSlashEvent) {
        val button = components.ephemeralButton(ButtonStyle.PRIMARY, "Retry (5 clicks in 1 minute)") {
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

    @AppDeclaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.slashCommand("rate_limit", function = ::onSlashRateLimit) {
//            rateLimit(bucketFactory)
            rateLimitReference(commandRateLimitGroup)
        }
    }

    @RateLimitDeclaration
    fun declare(rateLimitContainer: RateLimitContainer) {
        rateLimitContainer.rateLimit(commandRateLimitGroup, commandBucketFactory)
        rateLimitContainer.rateLimit(retryRateLimitGroup, retryBucketFactory)
    }
}