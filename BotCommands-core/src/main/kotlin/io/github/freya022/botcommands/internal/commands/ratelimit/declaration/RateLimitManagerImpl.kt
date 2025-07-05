package io.github.freya022.botcommands.internal.commands.ratelimit.declaration

import io.github.freya022.botcommands.api.commands.builder.RateLimitBuilder
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitManager
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.setCallerAsDeclarationSite
import io.github.freya022.botcommands.internal.commands.builder.RateLimitBuilderImpl
import io.github.freya022.botcommands.internal.commands.ratelimit.RateLimitContainer

internal class RateLimitManagerImpl internal constructor(override val context: BContext) : RateLimitManager() {
    private val container = context.getService<RateLimitContainer>()

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