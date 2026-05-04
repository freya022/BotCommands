package io.github.freya022.botcommands.internal.commands.text.ratelimit

import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.getChannel
import dev.freya02.botcommands.jda.ktx.requests.awaitCatching
import dev.freya02.botcommands.jda.ktx.requests.runIgnoringResponse
import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.commands.text.messages.TextCommandsMessages
import io.github.freya022.botcommands.api.commands.text.messages.TextCommandsMessagesFactory
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.commands.ratelimit.handler.DefaultRateLimitHandler
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.Instant
import kotlin.time.Duration.Companion.nanoseconds

internal class DefaultRateLimitHandlerRequestHandler : DefaultRateLimitHandler.RequestHandler {
    override suspend fun handle(
        instance: DefaultRateLimitHandler,
        context: RateLimitingContext,
        probe: ConsumptionProbe,
    ): Boolean {
        if (context is TextCommandRateLimitingContext) {
            val event = context.event
            val channel = when {
                event.guildChannel.canTalk() -> event.channel
                else -> event.author.openPrivateChannel().await()
            }
            val messages = context.context.getService<TextCommandsMessagesFactory>().get(event)
            val content = getRateLimitMessage(instance, event, messages, probe)

            runIgnoringResponse(ErrorResponse.CANNOT_SEND_TO_USER) {
                val messageId = channel.sendMessage(content).await().idLong
                if (instance.deleteOnRefill && channel is GuildChannel) {
                    val jda = channel.jda
                    val channelId = channel.idLong
                    DefaultRateLimitHandler.deleteScope.launch {
                        delay(probe.nanosToWaitForRefill.nanoseconds)
                        jda.getChannel<GuildMessageChannel>(channelId)
                            ?.deleteMessageById(messageId)
                            ?.awaitCatching()
                    }
                }
            }
            return true
        }
        return false
    }

    private fun getRateLimitMessage(
        instance: DefaultRateLimitHandler,
        event: MessageReceivedEvent,
        messages: TextCommandsMessages,
        probe: ConsumptionProbe
    ): MessageCreateData {
        val deadline = Instant.now().plusNanos(probe.nanosToWaitForRefill)
        return when (instance.scope) {
            RateLimitScope.USER -> messages.userRateLimited(event, deadline)
            RateLimitScope.USER_PER_GUILD -> messages.userRateLimited(event, deadline)
            RateLimitScope.USER_PER_CHANNEL -> messages.userRateLimited(event, deadline)
            RateLimitScope.GUILD -> messages.guildRateLimited(event, deadline)
            RateLimitScope.CHANNEL -> messages.channelRateLimited(event, deadline)
        }
    }
}
