package io.github.freya022.botcommands.api.commands.ratelimit

import io.github.freya022.botcommands.api.ReceiverConsumer
import io.github.freya022.botcommands.api.commands.annotations.Cooldown
import io.github.freya022.botcommands.api.commands.annotations.RateLimit
import io.github.freya022.botcommands.api.commands.annotations.RateLimitReference
import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.commands.builder.RateLimitBuilder
import io.github.freya022.botcommands.api.commands.ratelimit.annotations.RateLimitDeclaration
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketFactory
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.computeIfAbsentOrNull
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.internal.core.requiredFilter
import io.github.freya022.botcommands.internal.core.service.FunctionAnnotationsMap
import io.github.freya022.botcommands.internal.core.service.ServiceContainerImpl
import io.github.freya022.botcommands.internal.core.service.getParameters
import io.github.freya022.botcommands.internal.utils.FunctionFilter
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import io.github.freya022.botcommands.internal.utils.shortSignature
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.reflect.jvm.jvmErasure

private val logger = KotlinLogging.logger { }

/**
 * Contains all the rate limit handlers, either declared with [rateLimit],
 * [CommandBuilder.rateLimit], [CommandBuilder.rateLimitReference]
 * or annotations such as [@Cooldown][Cooldown], [@RateLimit][RateLimit]
 * and [@RateLimitDeclaration][RateLimitDeclaration].
 *
 * ### Rate limit cancellation
 * The rate limit can be cancelled inside the command with [CancellableRateLimit.cancelRateLimit] on your event.
 *
 * @see CommandBuilder.rateLimit
 * @see CommandBuilder.rateLimitReference
 * @see RateLimitDeclaration
 */
@BService
class RateLimitContainer internal constructor(serviceContainer: ServiceContainerImpl, functionAnnotationsMap: FunctionAnnotationsMap) {
    private val map: MutableMap<String, RateLimitInfo> = hashMapOf()

    init {
        val declarators = functionAnnotationsMap
            .get<RateLimitDeclaration>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(RateLimitContainer::class))
            .requiredFilter(FunctionFilter.blocking())
            .onEach { (instance, function) ->
                val args = serviceContainer.getParameters(function.nonInstanceParameters.drop(1).map { it.type.jvmErasure })
                function.call(instance, this, *args.toTypedArray())
            }

        if (logger.isTraceEnabled() && map.isNotEmpty()) {
            logger.trace {
                "Loaded ${map.size} rate limiters with:\n${declarators.joinAsList { it.function.shortSignature }}"
            }
        } else {
            logger.debug { "Loaded ${map.size} rate limiters" }
        }
    }

    @JvmSynthetic
    internal operator fun get(group: String): RateLimitInfo? = map[group]

    @JvmSynthetic
    internal operator fun contains(rateLimitGroup: String): Boolean = rateLimitGroup in map

    /**
     * Creates a rate limiter with the specified group.
     *
     * The created rate limiter can be used in [CommandBuilder.rateLimitReference] and [@RateLimitReference][RateLimitReference].
     *
     * @param group          The "name" of the rate limiter
     * @param bucketFactory  The bucket factory to use in [RateLimiterFactory]
     * @param limiterFactory The [RateLimiter] factory in charge of handling buckets and rate limits
     * @param block          Further configures the [RateLimitBuilder]
     *
     * @throws IllegalStateException If a rate limiter with the same group exists
     */
    fun rateLimit(
        group: String,
        bucketFactory: BucketFactory,
        limiterFactory: RateLimiterFactory = RateLimiter.defaultFactory(RateLimitScope.USER),
        block: ReceiverConsumer<RateLimitBuilder> = ReceiverConsumer.noop()
    ): RateLimitInfo {
        return checkNotNull(map.computeIfAbsentOrNull(group) { RateLimitBuilder(bucketFactory, limiterFactory).apply(block).build() }) {
            "A rate limiter already exists with a group of '$group'"
        }
    }
}