package io.github.freya022.botcommands.internal.commands.ratelimit

import io.github.freya022.botcommands.api.commands.annotations.*
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.Buckets
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.toSupplier
import io.github.freya022.botcommands.api.core.utils.findAnnotationRecursive
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.annotationRef
import io.github.freya022.botcommands.internal.utils.requireAt
import java.time.Duration
import kotlin.reflect.KFunction
import io.github.bucket4j.Bandwidth as BucketBandwidth

private fun Bandwidth.toRealBandwidth(): BucketBandwidth =
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

internal fun KFunction<*>.readRateLimit(): RateLimiter? {
    val rateLimitAnnotation = findAnnotationRecursive<RateLimit>() ?: this.declaringClass.findAnnotationRecursive<RateLimit>()
    val cooldownAnnotation = findAnnotationRecursive<Cooldown>() ?: this.declaringClass.findAnnotationRecursive<Cooldown>()
    requireAt(cooldownAnnotation == null || rateLimitAnnotation == null, this) {
        "Cannot use both ${annotationRef<Cooldown>()} and ${annotationRef<RateLimit>()}"
    }

    return if (rateLimitAnnotation != null) {
        val bucketConfigurationSupplier = Buckets.custom(rateLimitAnnotation.bandwidths.map { it.toRealBandwidth() }).toSupplier()
        RateLimiter.createDefault(rateLimitAnnotation.scope, bucketConfigurationSupplier, rateLimitAnnotation.deleteOnRefill)
    } else if (cooldownAnnotation != null) {
        val bucketConfigurationSupplier = Buckets.ofCooldown(Duration.of(cooldownAnnotation.cooldown, cooldownAnnotation.unit)).toSupplier()
        RateLimiter.createDefault(cooldownAnnotation.scope, bucketConfigurationSupplier, cooldownAnnotation.deleteOnRefill)
    } else {
        null
    }
}