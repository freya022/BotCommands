package io.github.freya022.botcommands.internal.ratelimit.builder

import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.api.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.api.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.ratelimit.declaration.RateLimitBuilder

internal class RateLimitBuilderImpl internal constructor(
    override val group: String,
    override val rateLimiter: RateLimiter
) : RateLimitBuilder {
    override lateinit var declarationSite: DeclarationSite

    internal fun build(): RateLimitInfo {
        return RateLimitInfo(group, rateLimiter, declarationSite)
    }
}
