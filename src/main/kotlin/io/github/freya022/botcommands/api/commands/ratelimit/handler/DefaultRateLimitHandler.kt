package io.github.freya022.botcommands.api.commands.ratelimit.handler

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.util.ref
import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.ratelimit.DefaultRateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.awaitCatching
import io.github.freya022.botcommands.api.core.utils.namedDefaultScope
import io.github.freya022.botcommands.api.core.utils.runIgnoringResponse
import io.github.freya022.botcommands.api.localization.DefaultMessages
import io.github.freya022.botcommands.api.localization.DefaultMessagesFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.TimeFormat
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds

private val deleteScope = namedDefaultScope("Rate limit message delete", 1)

/**
 * Default [RateLimitHandler] implementation based on [rate limit scopes][RateLimitScope].
 *
 * - Text command rate limits are sent to the user in the event's channel, if the bot cannot talk,
 *   then it is sent to the user's DMs, or returns if not possible.
 * - Interactions are simply replying an ephemeral message to the user.
 *
 * All messages sent to the user are localized messages from [DefaultMessages] and will be deleted when expired.
 *
 * **Note:** The rate limit message won't be deleted in a private channel,
 * or if the [refill delay][ConsumptionProbe.nanosToWaitForRefill] is longer than 10 minutes.
 *
 * @param scope          Scope of the rate limit, see [RateLimitScope] values.
 * @param deleteOnRefill Whether the rate limit message should be deleted after the [refill delay][ConsumptionProbe.nanosToWaitForRefill].
 *
 * @see DefaultRateLimiter
 * @see RateLimitScope
 */
class DefaultRateLimitHandler(
    private val scope: RateLimitScope,
    private val deleteOnRefill: Boolean = true
) : RateLimitHandler {
    override suspend fun onRateLimit(
        context: BContext,
        event: MessageReceivedEvent,
        commandInfo: TextCommandInfo,
        probe: ConsumptionProbe
    ) {
        val channel = when {
            event.guildChannel.canTalk() -> event.channel
            else -> event.author.openPrivateChannel().await()
        }
        val messages = context.getService<DefaultMessagesFactory>().get(event)
        val content = getRateLimitMessage(messages, probe)

        runIgnoringResponse(ErrorResponse.CANNOT_SEND_TO_USER) {
            val messageId = channel.sendMessage(content).await().idLong
            if (deleteOnRefill && channel is GuildChannel) {
                val channelRef by channel.ref()
                deleteScope.launch {
                    delay(probe.nanosToWaitForRefill.nanoseconds)
                    channelRef.deleteMessageById(messageId).awaitCatching()
                }
            }
        }
    }

    override suspend fun <T> onRateLimit(
        context: BContext,
        event: T,
        commandInfo: ApplicationCommandInfo,
        probe: ConsumptionProbe
    ) where T : GenericCommandInteractionEvent, T : IReplyCallback {
        onRateLimit(context, event, probe)
    }

    override suspend fun <T> onRateLimit(
        context: BContext,
        event: T,
        probe: ConsumptionProbe
    ) where T : GenericComponentInteractionCreateEvent, T : IReplyCallback, T : IMessageEditCallback {
        onRateLimit(context, event as IReplyCallback, probe)
    }

    private suspend fun onRateLimit(context: BContext, event: IReplyCallback, probe: ConsumptionProbe) {
        val messages = context.getService<DefaultMessagesFactory>().get(event)
        val content = getRateLimitMessage(messages, probe)
        val hook = event.reply(content).setEphemeral(true).await()
        // Only schedule delete if the interaction hook doesn't expire before
        // Technically this is supposed to be 15 minutes but, just to be safe
        if (deleteOnRefill && probe.nanosToWaitForRefill <= 10.minutes.inWholeNanoseconds) {
            deleteScope.launch {
                delay(probe.nanosToWaitForRefill.nanoseconds)
                hook.deleteOriginal().awaitCatching()
            }
        }
    }

    private fun getRateLimitMessage(
        messages: DefaultMessages,
        probe: ConsumptionProbe
    ): String {
        val timestamp = TimeFormat.RELATIVE.atTimestamp(System.currentTimeMillis() + probe.nanosToWaitForRefill.floorDiv(1_000_000))
        return when (scope) {
            RateLimitScope.USER -> messages.getUserRateLimitMsg(timestamp)
            RateLimitScope.USER_PER_GUILD -> messages.getUserRateLimitMsg(timestamp)
            RateLimitScope.USER_PER_CHANNEL -> messages.getUserRateLimitMsg(timestamp)
            RateLimitScope.GUILD -> messages.getGuildRateLimitMsg(timestamp)
            RateLimitScope.CHANNEL -> messages.getChannelRateLimitMsg(timestamp)
        }
    }
}