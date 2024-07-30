package io.github.freya022.botcommands.internal.commands.ratelimit.declaration

import io.github.freya022.botcommands.api.commands.builder.setCallerAsDeclarationSite
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiterFactory
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketFactory
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitManager
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.internal.commands.builder.RateLimitBuilderImpl
import io.github.freya022.botcommands.internal.commands.ratelimit.RateLimitContainer

internal class RateLimitManagerImpl internal constructor(override val context: BContext) : RateLimitManager() {
    private val container = context.getService<RateLimitContainer>()

    override fun createRateLimit(
        group: String,
        bucketFactory: BucketFactory,
        limiterFactory: RateLimiterFactory,
        block: RateLimitBuilderImpl.() -> Unit
    ): RateLimitInfo {
        val rateLimitInfo = RateLimitBuilderImpl(group, bucketFactory, limiterFactory)
            .setCallerAsDeclarationSite()
            .apply(block)
            .build()
        container[group] = rateLimitInfo

        return rateLimitInfo
    }
}