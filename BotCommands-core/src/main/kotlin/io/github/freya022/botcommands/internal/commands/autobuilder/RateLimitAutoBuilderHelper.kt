package io.github.freya022.botcommands.internal.commands.autobuilder

import io.github.bucket4j.Bandwidth as BucketBandwidth
import io.github.freya022.botcommands.api.commands.annotations.Bandwidth as BandwidthAnnotation
import io.github.freya022.botcommands.api.commands.annotations.Cooldown
import io.github.freya022.botcommands.api.commands.annotations.RateLimit
import io.github.freya022.botcommands.api.commands.annotations.RefillType
import io.github.freya022.botcommands.api.commands.ratelimit.AnnotatedRateLimiterFactory
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.Buckets
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.toSupplier
import io.github.freya022.botcommands.api.core.service.ServiceContainer
import io.github.freya022.botcommands.api.core.service.getService
import java.time.Duration

internal object RateLimitAutoBuilderHelper {
    fun readRateLimit(serviceContainer: ServiceContainer, rateLimitAnnotation: RateLimit): RateLimiter {
        val bucketConfigurationSupplier = Buckets.custom(rateLimitAnnotation.bandwidths.map { it.toRealBandwidth() }).toSupplier()
        val annotatedRateLimiterFactory = serviceContainer.getService<AnnotatedRateLimiterFactory>()
        return annotatedRateLimiterFactory.create(rateLimitAnnotation.scope, bucketConfigurationSupplier, rateLimitAnnotation.deleteOnRefill)
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

    fun readCooldown(serviceContainer: ServiceContainer, cooldownAnnotation: Cooldown): RateLimiter {
        val bucketConfigurationSupplier = Buckets.ofCooldown(Duration.of(cooldownAnnotation.cooldown, cooldownAnnotation.unit)).toSupplier()
        val annotatedRateLimiterFactory = serviceContainer.getService<AnnotatedRateLimiterFactory>()
        return annotatedRateLimiterFactory.create(cooldownAnnotation.scope, bucketConfigurationSupplier, cooldownAnnotation.deleteOnRefill)
    }
}
