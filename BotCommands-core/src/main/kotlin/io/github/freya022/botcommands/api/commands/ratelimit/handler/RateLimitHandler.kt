package io.github.freya022.botcommands.api.commands.ratelimit.handler

import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitingContext

/**
 * Defines the behavior when a rate limit is triggered.
 */
interface RateLimitHandler {
    suspend fun onRateLimit(context: RateLimitingContext, probe: ConsumptionProbe)
}
