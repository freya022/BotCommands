package io.github.freya022.botcommands.internal.commands.application.ratelimit

import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.commands.ratelimit.handler.DefaultRateLimitHandler

internal class DefaultRateLimitHandlerRequestHandler : DefaultRateLimitHandler.RequestHandler {
    override suspend fun handle(
        instance: DefaultRateLimitHandler,
        context: RateLimitingContext,
        probe: ConsumptionProbe,
    ): Boolean {
        if (context is ApplicationCommandRateLimitingContext) {
            instance.onInteractionRateLimit(context.context, context.event, probe)
            return true
        }
        return false
    }
}
