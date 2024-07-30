package io.github.freya022.botcommands.internal.commands.builder

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.CommandType
import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.commands.builder.DeclarationSite
import io.github.freya022.botcommands.api.commands.builder.RateLimitBuilder
import io.github.freya022.botcommands.api.commands.builder.setCallerAsDeclarationSite
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimiterFactory
import io.github.freya022.botcommands.api.commands.ratelimit.bucket.BucketFactory
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.service.getService
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.internal.commands.ratelimit.RateLimitContainer
import io.github.freya022.botcommands.internal.utils.lazyPath
import net.dv8tion.jda.api.Permission
import java.util.*

internal abstract class CommandBuilderImpl internal constructor(
    override val context: BContext,
    override val name: String
) : CommandBuilder {
    internal abstract val type: CommandType
    final override lateinit var declarationSite: DeclarationSite

    final override var userPermissions: EnumSet<Permission> = enumSetOf()

    final override var botPermissions: EnumSet<Permission> = enumSetOf()

    final override val path: CommandPath by lazyPath()

    internal var rateLimitInfo: RateLimitInfo? = null
        private set

    final override fun rateLimit(
        bucketFactory: BucketFactory,
        limiterFactory: RateLimiterFactory,
        block: RateLimitBuilder.() -> Unit
    ) {
        rateLimitInfo = RateLimitBuilder("$type: ${path.fullPath}", bucketFactory, limiterFactory)
            .setCallerAsDeclarationSite()
            .apply(block)
            .build()
    }

    final override fun rateLimitReference(group: String) {
        rateLimitInfo = context.getService<RateLimitContainer>()[group]
            ?: throw NoSuchElementException("Could not find a rate limiter for '$group'")
    }
}