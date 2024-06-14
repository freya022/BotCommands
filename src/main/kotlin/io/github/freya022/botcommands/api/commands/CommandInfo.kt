package io.github.freya022.botcommands.api.commands

import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.Permission
import java.util.*

interface CommandInfo : INamedCommand, IDeclarationSiteHolder, IRateLimitHolder {
    val context: BContext

    val userPermissions: EnumSet<Permission>
    val botPermissions: EnumSet<Permission>
}