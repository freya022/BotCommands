package io.github.freya022.botcommands.api.commands

import io.github.freya022.botcommands.api.commands.builder.IDeclarationSiteHolder
import io.github.freya022.botcommands.api.core.BContext
import net.dv8tion.jda.api.Permission
import java.util.*

/**
 * Represents a text/application command.
 */
interface CommandInfo : INamedCommand, IDeclarationSiteHolder, IRateLimitHolder {
    /**
     * The main context.
     */
    val context: BContext

    /**
     * The permissions required for the caller to use this command.
     */
    val userPermissions: EnumSet<Permission>

    /**
     * The permissions required for the bot to run this command.
     */
    val botPermissions: EnumSet<Permission>
}