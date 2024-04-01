package io.github.freya022.botcommands.internal.commands.ratelimit.declaration

import io.github.freya022.botcommands.api.commands.builder.RateLimitBuilder
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiterFactory
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketFactory
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitManager
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.annotations.IgnoreStackFrame
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.commands.ratelimit.RateLimitContainer

@IgnoreStackFrame
internal class RateLimitManagerImpl internal constructor(override val context: BContext) : RateLimitManager() {
    private val container = context.getService<RateLimitContainer>()

    override fun createRateLimit(
        group: String,
        bucketFactory: BucketFactory,
        limiterFactory: RateLimiterFactory,
        block: RateLimitBuilder.() -> Unit
    ): RateLimitInfo {
        return container.rateLimit(group, bucketFactory, limiterFactory, block)
    }
}