package io.github.freya022.botcommands.api.commands.ratelimit

import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.api.core.IDeclarationSiteHolder

/**
 * Container for rate limiters data.
 */
class RateLimitInfo internal constructor(
    val group: String,
    val limiter: RateLimiter,
    override val declarationSite: DeclarationSite
) : IDeclarationSiteHolder {
    override fun toString(): String {
        return "RateLimitInfo(group='$group', limiter=$limiter)"
    }
}