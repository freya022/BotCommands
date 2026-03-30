package io.github.freya022.botcommands.internal.components.ratelimit

import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.api.commands.ratelimit.handler.DefaultRateLimitHandler
import io.github.freya022.botcommands.api.components.ratelimit.ComponentRateLimitingContext

internal class DefaultRateLimitHandlerRequestHandler : DefaultRateLimitHandler.RequestHandler {
    override suspend fun handle(
        instance: DefaultRateLimitHandler,
        context: RateLimitingContext,
        probe: ConsumptionProbe,
    ): Boolean {
        if (context is ComponentRateLimitingContext) {
            instance.onInteractionRateLimit(context.context, context.event, probe)
            return true
        }
        return false
    }
}
