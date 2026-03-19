package io.github.freya022.botcommands.api.commands.ratelimit.handler

import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.getChannel
import dev.freya02.botcommands.jda.ktx.requests.awaitCatching
import dev.freya02.botcommands.jda.ktx.requests.runIgnoringResponse
import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.commands.ratelimit.*
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessages
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.namedDefaultScope
import io.github.freya022.botcommands.internal.utils.throwInternal
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.Event
import net.dv8tion.jda.api.events.interaction.GenericInteractionCreateEvent
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.Instant
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds

private val deleteScope = namedDefaultScope("Rate limit message delete", 1, isDaemon = true)

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

    private val handlers = ServiceLoader.load(RequestHandler::class.java) + RequestHandler { _, context, probe ->
        when (context) {
            is TextCommandRateLimitingContext -> {
                onRateLimit(context, probe)
                true
            }
            is ApplicationCommandRateLimitingContext -> {
                onInteractionRateLimit(context.context, context.event, probe)
                true
            }
            is ComponentRateLimitingContext -> {
                onInteractionRateLimit(context.context, context.event, probe)
                true
            }
            else -> false
        }
    }

    override suspend fun onRateLimit(context: RateLimitingContext, probe: ConsumptionProbe) {
        for (handler in handlers) {
            if (handler.handle(this, context, probe)) {
                return
            }
        }

        throwInternal("Unsupported context: ${context.javaClass.name}")
    }

    private suspend fun onRateLimit(
        rateLimitingContext: TextCommandRateLimitingContext,
        probe: ConsumptionProbe
    ) {
        val event = rateLimitingContext.event
        val channel = when {
            event.guildChannel.canTalk() -> event.channel
            else -> event.author.openPrivateChannel().await()
        }
        val messages = rateLimitingContext.context.getService<BotCommandsMessagesFactory>().get(event)
        val content = getRateLimitMessage(event, messages, probe)

        runIgnoringResponse(ErrorResponse.CANNOT_SEND_TO_USER) {
            val messageId = channel.sendMessage(content).await().idLong
            if (deleteOnRefill && channel is GuildChannel) {
                val jda = channel.jda
                val channelId = channel.idLong
                deleteScope.launch {
                    delay(probe.nanosToWaitForRefill.nanoseconds)
                    jda.getChannel<GuildMessageChannel>(channelId)
                        ?.deleteMessageById(messageId)
                        ?.awaitCatching()
                }
            }
        }
    }

    suspend fun <T> onInteractionRateLimit(
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
        messages: BotCommandsMessages,
        probe: ConsumptionProbe
    ): MessageCreateData {
        val deadline = Instant.now().plusNanos(probe.nanosToWaitForRefill)
        return when (scope) {
            RateLimitScope.USER -> messages.userRateLimited(event, deadline)
            RateLimitScope.USER_PER_GUILD -> messages.userRateLimited(event, deadline)
            RateLimitScope.USER_PER_CHANNEL -> messages.userRateLimited(event, deadline)
            RateLimitScope.GUILD -> messages.guildRateLimited(event, deadline)
            RateLimitScope.CHANNEL -> messages.channelRateLimited(event, deadline)
        }
    }

    fun interface RequestHandler {
        suspend fun handle(instance: DefaultRateLimitHandler, context: RateLimitingContext, probe: ConsumptionProbe): Boolean
    }
}
