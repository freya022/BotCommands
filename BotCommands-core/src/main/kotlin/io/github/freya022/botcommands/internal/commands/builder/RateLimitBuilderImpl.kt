package io.github.freya022.botcommands.internal.commands.builder

import io.github.freya022.botcommands.api.commands.builder.RateLimitBuilder
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.core.DeclarationSite

internal class RateLimitBuilderImpl internal constructor(
    override val group: String,
    override val rateLimiter: RateLimiter
) : RateLimitBuilder {
    override lateinit var declarationSite: DeclarationSite

    internal fun build(): RateLimitInfo {
        return RateLimitInfo(group, rateLimiter, declarationSite)
    }
}