package io.github.freya022.botcommands.internal.commands

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.builder.CommandBuilder
import io.github.freya022.botcommands.api.commands.builder.DeclarationSite
import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder
import io.github.freya022.botcommands.api.commands.ratelimit.RateLimitInfo
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand
import io.github.freya022.botcommands.internal.commands.mixins.INamedCommand.Companion.computePath
import io.github.freya022.botcommands.internal.commands.ratelimit.RateLimited
import net.dv8tion.jda.api.Permission
import java.util.*

abstract class AbstractCommandInfo internal constructor(
    builder: CommandBuilder
) : RateLimited, INamedCommand, IDeclarationSiteHolder {
    final override val name: String = builder.name
    final override val path: CommandPath by lazy { computePath() }

    final override val declarationSite: DeclarationSite = builder.declarationSite

    override val rateLimitInfo: RateLimitInfo? = builder.rateLimitInfo

    val userPermissions: EnumSet<Permission> = builder.userPermissions
    val botPermissions: EnumSet<Permission> = builder.botPermissions

    override fun toString(): String {
        return "${this::class.simpleName}: ${path.fullPath}"
    }
}