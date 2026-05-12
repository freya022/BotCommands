package io.github.freya022.botcommands.api.ratelimit.handler

import io.github.bucket4j.ConsumptionProbe
import io.github.freya022.botcommands.api.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.ratelimit.RateLimitingContext
import io.github.freya022.botcommands.internal.ratelimit.handler.DefaultRateLimitHandler

/**
 * Defines the behavior when a rate limit is triggered.
 */
interface RateLimitHandler {
    suspend fun onRateLimit(context: RateLimitingContext, probe: ConsumptionProbe)

    companion object {
        /**
         * Creates a default [RateLimitHandler] implementation based on [rate limit scopes][RateLimitScope].
         *
         * - Text command rate limits are sent to the user in the event's channel, if the bot cannot talk,
         *   then it is sent to the user's DMs, or returns if not possible.
         * - Interactions are simply replying an ephemeral message to the user.
         *
         * All messages sent to the user are localized messages using messages from the module's message pack and will be deleted when expired.
         *
         * **Note:** The rate limit message won't be deleted in a private channel,
         * or if the [refill delay][ConsumptionProbe.nanosToWaitForRefill] is longer than 10 minutes.
         *
         * @param scope          Scope of the rate limit, see [RateLimitScope] values.
         * @param deleteOnRefill Whether the rate limit message should be deleted after expiring, `true` by default
         *
         * @see RateLimitScope
         */
        @JvmStatic
        @JvmOverloads
        fun createDefault(scope: RateLimitScope, deleteOnRefill: Boolean = true): RateLimitHandler {
            return DefaultRateLimitHandler(scope, deleteOnRefill)
        }
    }
}
