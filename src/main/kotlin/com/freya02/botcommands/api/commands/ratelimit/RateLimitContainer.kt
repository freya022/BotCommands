package com.freya02.botcommands.api.commands.ratelimit

import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.commands.RateLimitScope
import com.freya02.botcommands.api.commands.annotations.Cooldown
import com.freya02.botcommands.api.commands.annotations.RateLimit
import com.freya02.botcommands.api.commands.annotations.RateLimitReference
import com.freya02.botcommands.api.commands.builder.CommandBuilder
import com.freya02.botcommands.api.commands.builder.RateLimitBuilder
import com.freya02.botcommands.api.commands.ratelimit.annotations.RateLimitDeclaration
import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketFactory
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.utils.computeIfAbsentOrNull
import com.freya02.botcommands.api.core.utils.joinAsList
import com.freya02.botcommands.internal.core.requiredFilter
import com.freya02.botcommands.internal.core.service.FunctionAnnotationsMap
import com.freya02.botcommands.internal.core.service.ServiceContainerImpl
import com.freya02.botcommands.internal.utils.FunctionFilter
import com.freya02.botcommands.internal.utils.ReflectionUtils.nonInstanceParameters
import com.freya02.botcommands.internal.utils.ReflectionUtils.shortSignature
import mu.KotlinLogging
import kotlin.reflect.jvm.jvmErasure

private val logger = KotlinLogging.logger { }

/**
 * Contains all the rate limit handlers, either declared with [rateLimit],
 * [CommandBuilder.rateLimit], [CommandBuilder.rateLimitReference]
 * or annotations such as [@Cooldown][Cooldown], [@RateLimit][RateLimit]
 * and [@RateLimitDeclaration][RateLimitDeclaration].
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
            .getFunctionsWithAnnotation<RateLimitDeclaration>()
            .requiredFilter(FunctionFilter.nonStatic())
            .requiredFilter(FunctionFilter.firstArg(RateLimitContainer::class))
            .requiredFilter(FunctionFilter.blocking())
            .onEach { (instance, function) ->
                val args = serviceContainer.getParameters(function.nonInstanceParameters.drop(1).map { it.type.jvmErasure })
                function.call(instance, this, *args.toTypedArray())
            }

        if (logger.isTraceEnabled) {
            logger.trace {
                "Loaded ${map.size} rate limiters with:\n${declarators.joinAsList { it.function.shortSignature }}"
            }
        } else {
            logger.debug { "Loaded ${map.size} rate limiters" }
        }
    }

    @JvmSynthetic
    internal operator fun get(group: String): RateLimitInfo? = map[group]

    /**
     * Creates a rate limiter with the specified group.
     *
     * The created rate limiter can be used in [CommandBuilder.rateLimitReference] and [@RateLimitReference][RateLimitReference].
     *
     * @param group          the "name" of the rate limiter
     * @param bucketFactory  the bucket factory to use in [RateLimiterFactory]
     * @param limiterFactory the [RateLimiter] factory in charge of handling buckets and rate limits
     * @param block          further configures the [RateLimitBuilder]
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