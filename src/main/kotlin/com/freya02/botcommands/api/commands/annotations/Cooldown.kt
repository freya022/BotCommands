package com.freya02.botcommands.api.commands.annotations

import com.freya02.botcommands.api.commands.RateLimitScope
import java.time.temporal.ChronoUnit

/**
 * Add a simple rate limit-based cooldown of this text / application command and components.
 *
 * @see RateLimit @RateLimit
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cooldown(
    /**
     * Cooldown time [in the specified unit][unit]
     * before the command can be used again in the scope specified by [rateLimitScope].
     *
     * @return Cooldown time [in the specified unit][unit]
     */
    val cooldown: Long = 0,

    /**
     * The time unit of the cooldown
     */
    val unit: ChronoUnit = ChronoUnit.MILLIS,

    /**
     * Scope of the cooldown, see [RateLimitScope] values.
     *
     * @see RateLimitScope
     */
    val rateLimitScope: RateLimitScope = RateLimitScope.USER
)
