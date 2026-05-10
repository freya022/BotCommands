package io.github.freya022.botcommands.internal.commands.builder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.CommandType
import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.setCallerAsDeclarationSite
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.api.ratelimit.RateLimiter
import io.github.freya022.botcommands.api.ratelimit.declaration.RateLimitBuilder
import io.github.freya022.botcommands.internal.commands.INamedCommandMixin
import io.github.freya022.botcommands.internal.ratelimit.RateLimitContainer
import io.github.freya022.botcommands.internal.ratelimit.builder.RateLimitBuilderImpl
import net.dv8tion.jda.api.Permission
import java.util.EnumSet

abstract class CommandBuilderImpl(
    override val context: BContext,
    override val name: String
) : CommandBuilder,
    INamedCommandMixin {

    abstract val type: CommandType
    final override lateinit var declarationSite: DeclarationSite

    final override var userPermissions: EnumSet<Permission> = enumSetOf()

    final override var botPermissions: EnumSet<Permission> = enumSetOf()

    final override val path: CommandPath by lazyPath()

    internal var rateLimitInfo: RateLimitInfo? = null
        private set

    final override fun rateLimit(
        rateLimiter: RateLimiter,
        block: RateLimitBuilder.() -> Unit
    ) {
        rateLimitInfo = RateLimitBuilderImpl("$type: ${path.fullPath}", rateLimiter)
            .setCallerAsDeclarationSite()
            .apply(block)
            .build()
    }

    final override fun rateLimitReference(group: String) {
        rateLimitInfo = context.getService<RateLimitContainer>()[group]
            ?: throw NoSuchElementException("Could not find a rate limiter for '$group'")
    }
}
