package io.github.freya022.botcommands.internal.commands.ratelimit

import io.github.freya022.botcommands.api.commands.annotations.*
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiterFactory
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketFactory
import io.github.freya022.botcommands.api.core.utils.simpleNestedName
import io.github.freya022.botcommands.internal.utils.ReflectionUtils.declaringClass
import io.github.freya022.botcommands.internal.utils.requireUser
import java.time.Duration
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import io.github.bucket4j.Bandwidth as BucketBandwidth
import io.github.bucket4j.Refill as BucketRefill

fun Refill.toRealRefill(): BucketRefill {
    val duration = Duration.of(period, periodUnit)
    return when (type) {
        RefillType.GREEDY -> BucketRefill.greedy(tokens, duration)
        RefillType.INTERVAL -> BucketRefill.intervally(tokens, duration)
    }
}

fun Bandwidth.toRealBandwidth(): BucketBandwidth {
    return BucketBandwidth.classic(capacity, refill.toRealRefill())
}

fun KFunction<*>.readRateLimit(): Pair<BucketFactory, RateLimiterFactory>? {
    val rateLimitAnnotation = findAnnotation<RateLimit>() ?: this.declaringClass.findAnnotation<RateLimit>()
    val cooldownAnnotation = findAnnotation<Cooldown>() ?: this.declaringClass.findAnnotation<Cooldown>()
    requireUser(cooldownAnnotation == null || rateLimitAnnotation == null, this) {
        "Cannot use both @${Cooldown::class.simpleNestedName} and @${RateLimit::class.simpleNestedName}"
    }

    return if (rateLimitAnnotation != null) {
        BucketFactory.custom(rateLimitAnnotation.bandwidths.map { it.toRealBandwidth() }) to RateLimiter.defaultFactory(rateLimitAnnotation.scope, rateLimitAnnotation.deleteOnRefill)
    } else if (cooldownAnnotation != null) {
        BucketFactory.ofCooldown(Duration.of(cooldownAnnotation.cooldown, cooldownAnnotation.unit)) to RateLimiter.defaultFactory(cooldownAnnotation.rateLimitScope, cooldownAnnotation.deleteOnRefill)
    } else {
        null
    }
}