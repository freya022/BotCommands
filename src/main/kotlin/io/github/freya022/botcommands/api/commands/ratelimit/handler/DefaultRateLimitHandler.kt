package io.github.freya022.botcommands.api.commands.ratelimit.handler

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.util.ref
import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.commands.application.ApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.text.TextCommandInfo
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessages
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.awaitCatching
import io.github.freya022.botcommands.api.core.utils.namedDefaultScope
import io.github.freya022.botcommands.api.core.utils.runIgnoringResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.events.interaction.command.GenericCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.callbacks.IMessageEditCallback
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.Instant
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
 * All messages sent to the user are localized messages from [BotCommandsMessages] and will be deleted when expired.
 *
 * **Note:** The rate limit message won't be deleted in a private channel,
 * or if the [refill delay][ConsumptionProbe.nanosToWaitForRefill] is longer than 10 minutes.
 *
 * @param scope          Scope of the rate limit, see [RateLimitScope] values.
 * @param deleteOnRefill Whether the rate limit message should be deleted after expiring
 *
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
        val messages = context.getService<BotCommandsMessagesFactory>().get(event)
        val content = getRateLimitMessage(event, messages, probe)

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
        onRateLimit0(context, event, probe)
    }

    override suspend fun <T> onRateLimit(
        context: BContext,
        event: T,
        probe: ConsumptionProbe
    ) where T : GenericComponentInteractionCreateEvent, T : IReplyCallback, T : IMessageEditCallback {
        onRateLimit0(context, event, probe)
    }

    private suspend fun <T> onRateLimit0(
        context: BContext,
        event: T,
        probe: ConsumptionProbe
    ) where T : GenericInteractionCreateEvent,
            T : IReplyCallback {
        val messages = context.getService<BotCommandsMessagesFactory>().get(event)
        val content = getRateLimitMessage(event, messages, probe)
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
        event: Event,
        replies: BotCommandsMessages,
        probe: ConsumptionProbe
    ): MessageCreateData {
        val deadline = Instant.now().plusNanos(probe.nanosToWaitForRefill)
        return when (scope) {
            RateLimitScope.USER -> replies.userRateLimited(event, deadline)
            RateLimitScope.USER_PER_GUILD -> replies.userRateLimited(event, deadline)
            RateLimitScope.USER_PER_CHANNEL -> replies.userRateLimited(event, deadline)
            RateLimitScope.GUILD -> replies.guildRateLimited(event, deadline)
            RateLimitScope.CHANNEL -> replies.channelRateLimited(event, deadline)
        }
    }
}