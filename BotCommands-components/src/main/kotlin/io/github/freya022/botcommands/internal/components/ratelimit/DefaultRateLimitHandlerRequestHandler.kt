package io.github.freya022.botcommands.internal.components.ratelimit

import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitingContext
import io.github.freya022.botcommands.api.core.messages.BotCommandsMessages
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.commands.ratelimit.handler.DefaultRateLimitHandler
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.time.Instant

internal class DefaultRateLimitHandlerRequestHandler : DefaultRateLimitHandler.RequestHandler {

    override suspend fun handle(
        instance: DefaultRateLimitHandler,
        context: RateLimitingContext,
        probe: ConsumptionProbe,
    ): Boolean {
        if (context is ComponentRateLimitingContext) {
            instance.onInteractionRateLimit(
                context.event,
                probe,
                messageGetter = { probe -> getRateLimitMessage(instance, context, probe) },
            )
            return true
        }
        return false
    }

    private fun getRateLimitMessage(
        instance: DefaultRateLimitHandler,
        context: ComponentRateLimitingContext,
        probe: ConsumptionProbe,
    ): MessageCreateData {
        val event = context.event
        val messages = context.context.getService<BotCommandsMessages>()

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
