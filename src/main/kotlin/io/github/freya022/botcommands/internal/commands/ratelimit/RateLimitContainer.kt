package io.github.freya022.botcommands.internal.commands.ratelimit

import io.github.freya022.botcommands.api.commands.builder.RateLimitBuilder
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiterFactory
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketFactory
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrow

@BService
internal class RateLimitContainer internal constructor() {
    private val infoByName: MutableMap<String, RateLimitInfo> = hashMapOf()

    internal val allInfos get() = infoByName.values
    internal val size get() = infoByName.size

    internal operator fun get(group: String): RateLimitInfo? = infoByName[group]

    internal operator fun contains(rateLimitGroup: String): Boolean = rateLimitGroup in infoByName

    internal fun rateLimit(group: String, bucketFactory: BucketFactory, limiterFactory: RateLimiterFactory, block: RateLimitBuilder.() -> Unit): RateLimitInfo {
        val rateLimitInfo = RateLimitBuilder(group, bucketFactory, limiterFactory).apply(block).build()
        infoByName.putIfAbsentOrThrow(group, rateLimitInfo) {
            "A rate limiter already exists with a group of '$group'"
        }
        return rateLimitInfo
    }
}