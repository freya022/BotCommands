package com.freya02.botcommands.internal.commands

import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.builder.CommandBuilder
import com.freya02.botcommands.api.commands.ratelimit.RateLimitInfo
import com.freya02.botcommands.internal.commands.mixins.INamedCommand
import com.freya02.botcommands.internal.commands.mixins.INamedCommand.Companion.computePath
import com.freya02.botcommands.internal.commands.ratelimit.RateLimited
import net.dv8tion.jda.api.Permission
import java.util.*

abstract class AbstractCommandInfo internal constructor(
    builder: CommandBuilder
) : RateLimited, INamedCommand {
    final override val name: String = builder.name
    final override val path: CommandPath by lazy { computePath() }

    override val rateLimitInfo: RateLimitInfo? = builder.rateLimitInfo

    val userPermissions: EnumSet<Permission> = builder.userPermissions
    val botPermissions: EnumSet<Permission> = builder.botPermissions

    override fun toString(): String {
        return "${this::class.simpleName}: ${path.fullPath}"
    }
}