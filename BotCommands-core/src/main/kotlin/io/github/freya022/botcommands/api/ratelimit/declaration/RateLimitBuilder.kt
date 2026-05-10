package io.github.freya022.botcommands.api.ratelimit.declaration

import io.github.freya022.botcommands.api.core.IDeclarationSiteHolderBuilder
import io.github.freya022.botcommands.api.ratelimit.RateLimiter
import io.github.freya022.botcommands.internal.ratelimit.annotations.RateLimitDSL

/**
 * Builder interface for rate limits.
 *
 * You can set a declaration site different from the default location (where the rate limit was created in the code) here.
 *
 * @see RateLimitManager.rateLimit
 */
@RateLimitDSL
interface RateLimitBuilder : IDeclarationSiteHolderBuilder {
    val group: String
    val rateLimiter: RateLimiter
}
