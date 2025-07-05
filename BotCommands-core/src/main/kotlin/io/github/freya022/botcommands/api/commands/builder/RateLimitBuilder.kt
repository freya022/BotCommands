package io.github.freya022.botcommands.api.commands.builder

import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitManager
import io.github.freya022.botcommands.api.core.IDeclarationSiteHolderBuilder
import io.github.freya022.botcommands.internal.commands.CommandDSL

/**
 * Builder interface for rate limits.
 *
 * You can set a declaration site different from the default location (where the rate limit was created in the code) here.
 *
 * @see CommandBuilder.rateLimit
 * @see RateLimitManager.rateLimit
 */
@CommandDSL
interface RateLimitBuilder : IDeclarationSiteHolderBuilder {
    val group: String
    val rateLimiter: RateLimiter
}
