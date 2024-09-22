package io.github.freya022.botcommands.api.commands.builder

import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.core.IDeclarationSiteHolderBuilder
import io.github.freya022.botcommands.internal.commands.CommandDSL

@CommandDSL
interface RateLimitBuilder : IDeclarationSiteHolderBuilder {
    val group: String
    val rateLimiter: RateLimiter
}
