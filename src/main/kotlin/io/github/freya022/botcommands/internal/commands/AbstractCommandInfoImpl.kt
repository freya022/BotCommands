package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.commands.CommandInfo
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.INamedCommand.Companion.computePath
import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.commands.builder.DeclarationSite
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.internal.commands.ratelimit.RateLimited
import net.dv8tion.jda.api.Permission
import java.util.*

internal abstract class AbstractCommandInfoImpl internal constructor(
    builder: CommandBuilder
) : CommandInfo,
    RateLimited {

    final override val name: String = builder.name
    final override val path: CommandPath by lazy { computePath() }

    final override val declarationSite: DeclarationSite = builder.declarationSite

    final override val rateLimitInfo: RateLimitInfo? = builder.rateLimitInfo

    final override val userPermissions: EnumSet<Permission> = builder.userPermissions
    final override val botPermissions: EnumSet<Permission> = builder.botPermissions

    override fun toString(): String {
        return "${this::class.simpleName}: ${path.fullPath}"
    }
}