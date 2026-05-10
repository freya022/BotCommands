package io.github.freya022.botcommands.internal.commands.autobuilder

import io.github.bucket4j.Bandwidth as BucketBandwidth
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.api.ratelimit.AnnotatedRateLimiterFactory
import io.github.freya022.botcommands.api.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.ratelimit.annotations.Bandwidth as BandwidthAnnotation
import io.github.freya022.botcommands.api.ratelimit.annotations.Cooldown
import io.github.freya022.botcommands.api.ratelimit.annotations.RateLimit
import io.github.freya022.botcommands.api.ratelimit.annotations.RefillType
import io.github.freya022.botcommands.api.ratelimit.bucket.Buckets
import io.github.freya022.botcommands.api.ratelimit.bucket.toSupplier
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.requireAt
import java.time.Duration
import kotlin.reflect.KFunction

internal interface RateLimitAutoBuilderHelper {
    val serviceContainer: ServiceContainer

    fun readRateLimit(func: KFunction<*>): RateLimiter? {
        val rateLimitAnnotation = func.findAnnotationRecursive<RateLimit>() ?: func.declaringClass.findAnnotationRecursive<RateLimit>()
        val cooldownAnnotation = func.findAnnotationRecursive<Cooldown>() ?: func.declaringClass.findAnnotationRecursive<Cooldown>()
        requireAt(cooldownAnnotation == null || rateLimitAnnotation == null, func) {
            "Cannot use both ${annotationRef<Cooldown>()} and ${annotationRef<RateLimit>()}"
        }

        return if (rateLimitAnnotation != null) {
            val bucketConfigurationSupplier = Buckets.custom(rateLimitAnnotation.bandwidths.map { it.toRealBandwidth() }).toSupplier()
            val annotatedRateLimiterFactory = serviceContainer.getService<AnnotatedRateLimiterFactory>()
            annotatedRateLimiterFactory.create(rateLimitAnnotation.scope, bucketConfigurationSupplier, rateLimitAnnotation.deleteOnRefill)
        } else if (cooldownAnnotation != null) {
            val bucketConfigurationSupplier = Buckets.ofCooldown(Duration.of(cooldownAnnotation.cooldown, cooldownAnnotation.unit)).toSupplier()
            val annotatedRateLimiterFactory = serviceContainer.getService<AnnotatedRateLimiterFactory>()
            annotatedRateLimiterFactory.create(cooldownAnnotation.scope, bucketConfigurationSupplier, cooldownAnnotation.deleteOnRefill)
        } else {
            null
        }
    }

    private fun BandwidthAnnotation.toRealBandwidth(): BucketBandwidth =
        BucketBandwidth.builder()
            .capacity(capacity)
            .let {
                val duration = Duration.of(refill.period, refill.periodUnit)
                when (refill.type) {
                    RefillType.GREEDY -> it.refillGreedy(refill.tokens, duration)
                    RefillType.INTERVAL -> it.refillIntervally(refill.tokens, duration)
                }
            }
            .build()
}
