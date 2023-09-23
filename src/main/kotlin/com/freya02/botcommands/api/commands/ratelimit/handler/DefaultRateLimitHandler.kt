package com.freya02.botcommands.api.commands.ratelimit.handler

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.DefaultMessages
import com.freya02.botcommands.api.commands.RateLimitScope
import com.freya02.botcommands.api.commands.ratelimit.DefaultRateLimiter
import com.freya02.botcommands.internal.commands.application.ApplicationCommandInfo
import com.freya02.botcommands.internal.commands.prefixed.TextCommandInfo
import dev.minn.jda.ktx.coroutines.await
import io.github.bucket4j.ConsumptionProbe
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.ErrorResponse

/**
 * Default [RateLimitHandler] implementation based on [rate limit scopes][RateLimitScope].
 *
 * - Text command rate limits are sent to the user in the event's channel, if the bot cannot talk,
 *   then it is sent to the user's DMs, or returns if not possible.
 * - Interactions are simply replying an ephemeral message to the user.
 *
 * All messages sent to the user are localized messages from [DefaultMessages].
 *
 * @see DefaultRateLimiter
 */
class DefaultRateLimitHandler(private val scope: RateLimitScope) : RateLimitHandler {
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
            channel.sendMessage(content).await()
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
        event.reply(content).setEphemeral(true).await()
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