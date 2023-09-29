package com.freya02.botcommands.api.commands.builder

import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.RateLimitScope
import com.freya02.botcommands.api.commands.annotations.RateLimit
import com.freya02.botcommands.api.commands.annotations.RateLimitReference
import com.freya02.botcommands.api.commands.ratelimit.RateLimitContainer
import com.freya02.botcommands.api.commands.ratelimit.RateLimitInfo
import com.freya02.botcommands.api.commands.ratelimit.RateLimiter
import com.freya02.botcommands.api.commands.ratelimit.RateLimiterFactory
import com.freya02.botcommands.api.commands.ratelimit.annotations.RateLimitDeclaration
import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketFactory
import com.freya02.botcommands.api.core.service.getService
import com.freya02.botcommands.api.core.utils.enumSetOf
import com.freya02.botcommands.internal.BContextImpl
import com.freya02.botcommands.internal.commands.CommandDSL
import com.freya02.botcommands.internal.commands.mixins.INamedCommand
import com.freya02.botcommands.internal.commands.mixins.INamedCommand.Companion.computePath
import net.dv8tion.jda.api.Permission
import java.util.*
import kotlin.time.Duration

@CommandDSL
abstract class CommandBuilder internal constructor(protected val context: BContextImpl, override val name: String) : INamedCommand {
    var userPermissions: EnumSet<Permission> = enumSetOf()
    var botPermissions: EnumSet<Permission> = enumSetOf()

    final override val path: CommandPath by lazy { computePath() }

    internal var rateLimitInfo: RateLimitInfo? = null
        private set

    /**
     * Creates a rate limiter with the specified group.
     *
     * @param bucketFactory  the bucket factory to use in [RateLimiterFactory]
     * @param limiterFactory the [RateLimiter] factory in charge of handling buckets and rate limits
     * @param block          further configures the [RateLimitBuilder]
     *
     * @throws IllegalStateException If a rate limiter with the same group exists
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
        rateLimitInfo = context.getService<RateLimitContainer>().rateLimit(path.fullPath, bucketFactory, limiterFactory, block)
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

fun CommandBuilder.cooldown(scope: RateLimitScope, duration: Duration, block: ReceiverConsumer<RateLimitBuilder> = ReceiverConsumer.noop()) =
    rateLimit(BucketFactory.ofCooldown(duration), RateLimiter.defaultFactory(scope), block)
