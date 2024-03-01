package io.github.freya022.botcommands.internal.commands.ratelimit.declaration

import io.github.freya022.botcommands.api.commands.ratelimit.declaration.RateLimitProvider
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.annotations.BService
import io.github.freya022.botcommands.api.core.utils.joinAsList
import io.github.freya022.botcommands.internal.commands.ratelimit.RateLimitContainer
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger { }

@BService(priority = 1) //Higher than all command declarations
internal class RateLimitDeclarationRunner internal constructor(
    context: BContext,
    rateLimitProviders: List<RateLimitProvider>,
    rateLimitContainer: RateLimitContainer
) {
    init {
        val manager = RateLimitManagerImpl(context)
        rateLimitProviders.forEach { it.declareRateLimit(manager) }

        if (logger.isTraceEnabled()) {
            logger.trace {
                "Registered ${rateLimitContainer.size} rate limiters:\n${rateLimitContainer.allInfos.joinAsList()}"
            }
        } else {
            logger.debug { "Registered ${rateLimitContainer.size} rate limiters" }
        }
    }
}