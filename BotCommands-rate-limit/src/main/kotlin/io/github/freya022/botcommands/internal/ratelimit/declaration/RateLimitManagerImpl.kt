package io.github.freya022.botcommands.internal.ratelimit.declaration

import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.setCallerAsDeclarationSite
import io.github.freya022.botcommands.api.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.api.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.ratelimit.declaration.RateLimitBuilder
import io.github.freya022.botcommands.api.ratelimit.declaration.RateLimitManager
import io.github.freya022.botcommands.internal.ratelimit.RateLimitContainer
import io.github.freya022.botcommands.internal.ratelimit.builder.RateLimitBuilderImpl

internal class RateLimitManagerImpl internal constructor(
    override val context: BContext,
    private val container: RateLimitContainer,
) : RateLimitManager() {

    override fun createRateLimit(
        group: String,
        rateLimiter: RateLimiter,
        block: RateLimitBuilder.() -> Unit
    ): RateLimitInfo {
        val rateLimitInfo = RateLimitBuilderImpl(group, rateLimiter)
            .setCallerAsDeclarationSite()
            .apply(block)
            .build()
        container[group] = rateLimitInfo

        return rateLimitInfo
    }
}
