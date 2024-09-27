@file:IgnoreStackFrame // Due to extensions

package io.github.freya022.botcommands.api.commands.builder

import io.github.freya022.botcommands.api.commands.INamedCommand
import io.github.freya022.botcommands.api.commands.annotations.Cooldown
import io.github.freya022.botcommands.api.commands.annotations.RateLimit
import io.github.freya022.botcommands.api.commands.annotations.RateLimitReference
import io.github.freya022.botcommands.api.commands.ratelimit.CancellableRateLimit
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitScope
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.Buckets
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.toSupplier
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitProvider
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.BotOwners
import io.github.freya022.botcommands.api.core.IDeclarationSiteHolderBuilder
import io.github.freya022.botcommands.api.core.annotations.IgnoreStackFrame
import io.github.freya022.botcommands.internal.commands.CommandDSL
import net.dv8tion.jda.api.Permission
import java.util.*
import kotlin.time.Duration

@CommandDSL
interface CommandBuilder : INamedCommand, IDeclarationSiteHolderBuilder {
    /**
     * The main context.
     */
    val context: BContext

    /**
     * The permissions required for the caller to use this command.
     */
    var userPermissions: EnumSet<Permission>

    /**
     * The permissions required for the bot to run this command.
     */
    var botPermissions: EnumSet<Permission>

    /**
     * Sets an anonymous rate limiter on this command.
     * This rate limiter cannot be referenced anywhere else as it is not registered.
     *
     * **Note:** This won't apply if you are a [bot owner][BotOwners.isOwner].
     *
     * ### Rate limit cancellation
     * The rate limit can be cancelled inside the command with [CancellableRateLimit.cancelRateLimit] on your event.
     *
     * ### Example
     *
     * ```kt
     * @Command
     * class SlashSkip : GlobalApplicationCommandProvider {
     *     suspend fun onSlashSkip(event: GuildSlashEvent) {
     *         // Handle command
     *     }
     *
     *     override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
     *         manager.slashCommand("skip", function = ::onSlashSkip) {
     *             val bucketConfiguration = Buckets.spikeProtected(
     *                 capacity = 5,
     *                 duration = 1.minutes,
     *                 spikeCapacity = 2,
     *                 spikeDuration = 5.seconds
     *             )
     *
     *             rateLimit(RateLimiter.createDefault(RateLimitScope.USER, bucketConfiguration.toSupplier(), deleteOnRefill = true))
     *         }
     *     }
     * }
     * ```
     *
     * @param rateLimiter The [RateLimiter] in charge of retrieving buckets and handling rate limits
     * @param block       Further configures the [RateLimitBuilder]
     *
     * @see RateLimit @RateLimit
     * @see RateLimitProvider
     */
    fun rateLimit(rateLimiter: RateLimiter, block: RateLimitBuilder.() -> Unit = {})

    /**
     * Sets the rate limiter of this command to one declared by a [RateLimitProvider].
     *
     * @throws NoSuchElementException If the rate limiter with the given group cannot be found
     *
     * @see RateLimitReference @RateLimitReference
     */
    fun rateLimitReference(group: String)
}

/**
 * Sets an anonymous rate limit-based cooldown on this command.
 * This cooldown cannot be referenced anywhere else as it is not registered.
 *
 * ### Cooldown cancellation
 * The cooldown can be cancelled inside the command with [CancellableRateLimit.cancelRateLimit] on your event.
 *
 * @param duration       The duration before the cooldown expires
 * @param scope          The scope of the cooldown
 * @param deleteOnRefill Whether the cooldown message should be deleted after the cooldown expires
 * @param block          Further configures the [RateLimitBuilder]
 *
 * @see Cooldown @Cooldown
 * @see CommandBuilder.rateLimit
 */
fun CommandBuilder.cooldown(
    duration: Duration,
    scope: RateLimitScope = RateLimitScope.USER,
    deleteOnRefill: Boolean = true,
    block: RateLimitBuilder.() -> Unit = {}
) = rateLimit(RateLimiter.createDefault(scope, Buckets.ofCooldown(duration).toSupplier(), deleteOnRefill), block)
