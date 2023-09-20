package com.freya02.botcommands.api.commands.annotations

import com.freya02.botcommands.api.commands.RateLimitScope
import java.time.temporal.ChronoUnit

enum class RefillType {
    GREEDY,
    INTERVAL
}

@Target
@Retention(AnnotationRetention.RUNTIME)
annotation class Bandwidth(val capacity: Long, val refill: Refill)

@Target
@Retention(AnnotationRetention.RUNTIME)
annotation class Refill(val type: RefillType, val tokens: Long, val period: Long, val periodUnit: ChronoUnit)

//TODO docs
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimit(
    val scope: RateLimitScope,
    val baseBandwidth: Bandwidth,
    val spikeBandwidth: Bandwidth
)

//TODO docs
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RateLimitReference(val group: String)