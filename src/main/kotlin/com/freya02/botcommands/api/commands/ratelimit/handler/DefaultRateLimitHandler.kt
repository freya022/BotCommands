package com.freya02.botcommands.api.commands.ratelimit.handler

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.DefaultMessages
import com.freya02.botcommands.api.commands.RateLimitScope
import com.freya02.botcommands.api.commands.ratelimit.DefaultRateLimiter
import com.freya02.botcommands.api.core.utils.namedDefaultScope
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo
import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.util.ref
import io.github.bucket4j.ConsumptionProbe
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.ErrorResponse
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
 * **Note:** the rate limit message won't be deleted in a private channel,
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
        val messages = context.getDefaultMessages(event.guild)
        val content = getRateLimitMessage(messages, probe)

        try {
            val messageId = channel.sendMessage(content).await().idLong
            if (deleteOnRefill && channel is GuildChannel) {
                val channelRef by channel.ref()
                deleteScope.launch {
                    delay(probe.nanosToWaitForRefill.nanoseconds)
                    runCatching { channelRef.deleteMessageById(messageId).await() }
                }
            }
        } catch (e: Exception) {
            if (e is ErrorResponseException && e.errorResponse == ErrorResponse.CANNOT_SEND_TO_USER) {
                // Ignore
            } else {
                throw e
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
        val messages = context.getDefaultMessages(event)
        val content = getRateLimitMessage(messages, probe)
        val hook = event.reply(content).setEphemeral(true).await()
        // Only schedule delete if the interaction hook doesn't expire before
        // Technically this is supposed to be 15 minutes but, just to be safe
        if (deleteOnRefill && probe.nanosToWaitForRefill <= 10.minutes.inWholeNanoseconds) {
            deleteScope.launch {
                delay(probe.nanosToWaitForRefill.nanoseconds)
                runCatching { hook.deleteOriginal().await() }
            }
        }
    }

    private fun getRateLimitMessage(
        messages: DefaultMessages,
        probe: ConsumptionProbe
    ): String = when (scope) {
        RateLimitScope.USER -> messages.getUserCooldownMsg(probe.nanosToWaitForRefill / 1_000_000_000.0)
        RateLimitScope.USER_PER_GUILD -> messages.getUserCooldownMsg(probe.nanosToWaitForRefill / 1_000_000_000.0)
        RateLimitScope.USER_PER_CHANNEL -> messages.getUserCooldownMsg(probe.nanosToWaitForRefill / 1_000_000_000.0)
        RateLimitScope.GUILD -> messages.getGuildCooldownMsg(probe.nanosToWaitForRefill / 1_000_000_000.0)
        RateLimitScope.CHANNEL -> messages.getChannelCooldownMsg(probe.nanosToWaitForRefill / 1_000_000_000.0)
    }
}