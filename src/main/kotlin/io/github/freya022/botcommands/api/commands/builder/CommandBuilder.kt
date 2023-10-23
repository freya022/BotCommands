package io.github.freya022.botcommands.api.commands.builder

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.CommandType
import io.github.freya022.botcommands.api.commands.annotations.Cooldown
import io.github.freya022.botcommands.api.commands.annotations.RateLimit
import io.github.freya022.botcommands.api.commands.annotations.RateLimitReference
import io.github.freya022.botcommands.api.commands.ratelimit.*
import io.github.freya022.botcommands.api.commands.ratelimit.annotations.RateLimitDeclaration
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketFactory
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
     * Creates a rate limiter.
     *
     * ### Rate limit cancellation
     * The rate limit can be cancelled inside the command with [CancellableRateLimit.cancelRateLimit] on your event.
     *
     * @param bucketFactory  the bucket factory to use in [RateLimiterFactory]
     * @param limiterFactory the [RateLimiter] factory in charge of handling buckets and rate limits
     * @param block          further configures the [RateLimitBuilder]
     *
     * @see RateLimit @RateLimit
     * @see RateLimitContainer
     * @see RateLimitDeclaration
     */
    fun rateLimit(
        bucketFactory: BucketFactory,
        limiterFactory: RateLimiterFactory = RateLimiter.defaultFactory(RateLimitScope.USER),
        block: ReceiverConsumer<RateLimitBuilder> = ReceiverConsumer.noop()
    ) {
        rateLimitInfo = context.getService<RateLimitContainer>().rateLimit("$type: ${path.fullPath}", bucketFactory, limiterFactory, block)
    }

    /**
     * Sets the rate limiter of this command to one declared by [@RateLimitDeclaration][RateLimitDeclaration].
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
 * Creates a rate limit-based cooldown.
 *
 * ### Cooldown cancellation
 * The cooldown can be cancelled inside the command with [CancellableRateLimit.cancelRateLimit] on your event.
 *
 * @param scope    the scope of the cooldown
 * @param duration the duration before the cooldown expires
 * @param block    further configures the [RateLimitBuilder]
 *
 * @see Cooldown @Cooldown
 * @see CommandBuilder.rateLimit
 */
fun CommandBuilder.cooldown(scope: RateLimitScope, duration: Duration, block: ReceiverConsumer<RateLimitBuilder> = ReceiverConsumer.noop()) =
    rateLimit(BucketFactory.ofCooldown(duration), RateLimiter.defaultFactory(scope), block)
