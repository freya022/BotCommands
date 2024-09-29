package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.commands.CommandInfo
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.api.core.DeclarationSite
import io.github.freya022.botcommands.internal.commands.builder.CommandBuilderImpl
import io.github.freya022.botcommands.internal.utils.lazyPath
import net.dv8tion.jda.api.Permission
import java.util.*

internal abstract class AbstractCommandInfoImpl internal constructor(
    builder: CommandBuilderImpl
) : CommandInfo {

    final override val name: String = builder.name
    final override val path: CommandPath by lazyPath()

    final override val declarationSite: DeclarationSite = builder.declarationSite

    internal val rateLimitInfo: RateLimitInfo? = builder.rateLimitInfo
    final override fun hasRateLimiter(): Boolean = rateLimitInfo != null

    final override val userPermissions: EnumSet<Permission> = builder.userPermissions
    final override val botPermissions: EnumSet<Permission> = builder.botPermissions

    override fun toString(): String {
        return "${this::class.simpleName}: ${path.fullPath}"
    }
}