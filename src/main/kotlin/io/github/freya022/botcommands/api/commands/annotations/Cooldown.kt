package io.github.freya022.botcommands.api.commands.annotations

import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.commands.builder.cooldown
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitManager
import io.github.freya022.botcommands.api.core.BotOwners
import java.time.temporal.ChronoUnit

/**
 * Add a simple rate limit-based cooldown of this text / application command and components.
 *
 * **Note:** This won't apply if you are a [bot owner][BotOwners.isOwner].
 *
 * **Text commands note:** This applies to the command itself, not only this variation,
 * in other words, this applies to all commands with the same path.
 *
 * ### Cooldown cancellation
 * The cooldown can be cancelled inside the command with [CancellableRateLimit.cancelRateLimit] on your event.
 *
 * @see RateLimit @RateLimit
 *
 * @see CommandBuilder.cooldown In-command equivalent
 * @see RateLimitManager.cooldown Code-declared equivalent
 */
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cooldown(
    /**
     * Cooldown time [in the specified unit][unit]
     * before the command can be used again in the scope specified by [scope].
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
    val scope: RateLimitScope = RateLimitScope.USER,

    /**
     * Whether the cooldown message should be deleted after the cooldown has expired.
     *
     * **Note:** The cooldown message won't be deleted in a private channel,
     * or if the cooldown is longer than 10 minutes.
     *
     * **Default:** `true`
     */
    val deleteOnRefill: Boolean = true
)
