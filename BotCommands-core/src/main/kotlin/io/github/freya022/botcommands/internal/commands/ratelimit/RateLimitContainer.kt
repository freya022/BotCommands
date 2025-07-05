package io.github.freya022.botcommands.internal.commands.ratelimit

import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.internal.utils.putIfAbsentOrThrow

@BService
internal class RateLimitContainer internal constructor() {
    private val infoByName: MutableMap<String, RateLimitInfo> = hashMapOf()

    internal val allInfos get() = infoByName.values
    internal val size get() = infoByName.size

    internal operator fun get(group: String): RateLimitInfo? = infoByName[group]

    internal operator fun contains(rateLimitGroup: String): Boolean = rateLimitGroup in infoByName

    internal operator fun set(group: String, rateLimitInfo: RateLimitInfo) {
        infoByName.putIfAbsentOrThrow(group, rateLimitInfo) {
            "A rate limiter already exists with a group of '$group'"
        }
    }
}