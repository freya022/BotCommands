package com.freya02.botcommands.api.commands.ratelimit

import com.freya02.botcommands.api.ReceiverConsumer
import com.freya02.botcommands.api.apply
import com.freya02.botcommands.api.commands.RateLimitScope
import com.freya02.botcommands.api.commands.builder.RateLimitBuilder
import com.freya02.botcommands.api.commands.ratelimit.bucket.BucketFactory
import com.freya02.botcommands.api.core.service.annotations.BService
import com.freya02.botcommands.api.core.utils.computeIfAbsentOrNull
import com.freya02.botcommands.internal.utils.throwUser

@BService
class RateLimitContainer internal constructor() {
    //TODO rate limit declaration using @RateLimitDeclaration
    private val map: MutableMap<String, RateLimitInfo> = hashMapOf()

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