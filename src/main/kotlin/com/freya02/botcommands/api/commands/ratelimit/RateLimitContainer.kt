package com.freya02.botcommands.api.commands.ratelimit

import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.apply
import com.freya02.botcommands.api.commands.RateLimitScope
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
import com.freya02.botcommands.internal.utils.throwUser
import mu.KotlinLogging
import kotlin.reflect.jvm.jvmErasure

private val logger = KotlinLogging.logger { }

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

    operator fun get(group: String): RateLimitInfo? = map[group]

    fun rateLimit(
        group: String,
        bucketFactory: BucketFactory,
        helperFactory: RateLimitHelperFactory = RateLimitHelper.defaultFactory(RateLimitScope.USER),
        block: ReceiverConsumer<RateLimitBuilder> = ReceiverConsumer { }
    ): RateLimitInfo {
        return map.computeIfAbsentOrNull(group) { RateLimitBuilder(bucketFactory, helperFactory).apply(block).build() }
            ?: throwUser("A rate limiter already exists with a group of '$group'")
    }
}