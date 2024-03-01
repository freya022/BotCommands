package io.github.freya022.botcommands.api.commands.builder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.CommandType
import io.github.freya022.botcommands.api.commands.annotations.Cooldown
import io.github.freya022.botcommands.api.commands.annotations.RateLimit
import io.github.freya022.botcommands.api.commands.annotations.RateLimitReference
import io.github.freya022.botcommands.api.commands.ratelimit.*
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketFactory
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitProvider
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.internal.commands.CommandDSL
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand.Companion.computePath
import net.dv8tion.jda.api.Permission
import java.util.*
import kotlin.time.Duration

@CommandDSL
abstract class CommandBuilder internal constructor(val context: BContext, override val name: String) : INamedCommand {
    internal abstract val type: CommandType

    var userPermissions: EnumSet<Permission> = enumSetOf()
    var botPermissions: EnumSet<Permission> = enumSetOf()

    final override val path: CommandPath by lazy { computePath() }

    internal var rateLimitInfo: RateLimitInfo? = null
        private set

    /**
     * Sets an anonymous rate limiter on this command.
     * This rate limiter cannot be referenced anywhere else as it is not registered.
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
     *             val bucketFactory = BucketFactory.spikeProtected(
     *                 capacity = 5,
     *                 duration = 1.minutes,
     *                 spikeCapacity = 2,
     *                 spikeDuration = 5.seconds
     *             )
     *
     *             // Defaults to the USER rate limit scope
     *             rateLimit(bucketFactory)
     *         }
     *     }
     * }
     * ```
     *
     * @param bucketFactory  The bucket factory to use in [RateLimiterFactory]
     * @param limiterFactory The [RateLimiter] factory in charge of handling buckets and rate limits
     * @param block          Further configures the [RateLimitBuilder]
     *
     * @see RateLimit @RateLimit
     * @see RateLimitProvider
     */
    fun rateLimit(
        bucketFactory: BucketFactory,
        limiterFactory: RateLimiterFactory = RateLimiter.defaultFactory(RateLimitScope.USER),
        block: RateLimitBuilder.() -> Unit = {}
    ) {
        rateLimitInfo = RateLimitBuilder("$type: ${path.fullPath}", bucketFactory, limiterFactory).apply(block).build()
    }

    // Different specifications with the same group will not exist
    // as the function is only called if a single rate limit annotation is on a given command path,
    // see singleValueOfVariants.
    // A rate limiter may already exist in case a command gets declared more than once,
    // such as guild commands, or after a command update.
    internal fun rateLimitIfAbsent(bucketFactory: BucketFactory, limiterFactory: RateLimiterFactory) {
        val rateLimitContainer = context.getService<RateLimitContainer>()
        val group = "$type: ${path.fullPath}"
        // Take existing info if rate limiter already exists
        rateLimitInfo = rateLimitContainer[group]
            ?: rateLimitContainer.rateLimit(group, bucketFactory, limiterFactory) {}
    }

    /**
     * Sets the rate limiter of this command to one declared by a [RateLimitProvider].
     *
     * @throws NoSuchElementException If the rate limiter with the given group cannot be found
     *
     * @see RateLimitReference @RateLimitReference
     */
    fun rateLimitReference(group: String) {
        rateLimitInfo = context.getService<RateLimitContainer>()[group]
            ?: throw NoSuchElementException("Could not find a rate limiter for '$group'")
    }
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
 * @param deleteOnRefill Whether the cooldown messages should be deleted after the cooldown expires
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
) = rateLimit(BucketFactory.ofCooldown(duration), RateLimiter.defaultFactory(scope, deleteOnRefill), block)
