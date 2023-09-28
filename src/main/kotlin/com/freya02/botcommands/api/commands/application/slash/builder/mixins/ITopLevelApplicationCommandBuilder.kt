package com.freya02.botcommands.api.commands.application.slash.builder.mixins

import com.freya02.botcommands.api.commands.annotations.UserPermissions
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import com.freya02.botcommands.api.commands.application.context.annotations.JDAUserCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import net.dv8tion.jda.api.Permission

interface ITopLevelApplicationCommandBuilder {
    /**
     * @see JDASlashCommand.scope
     * @see JDAUserCommand.scope
     * @see JDAMessageCommand.scope
     */
    val scope: CommandScope

    /**
     * Specifies whether the application command is disabled for everyone but administrators by default,
     * so that administrators can further configure the command.
     *
     * **Note:** You cannot use this with [UserPermissions].
     *
     * For example, maybe you want a ban command to be usable by someone who has a certain role,
     * but which doesn't have the [BAN_MEMBERS][Permission.BAN_MEMBERS] permission,
     * you would then default lock the command and let the admins of the guild configure it
     *
     * **Default:** false
     *
     * @return `true` if the command should be disabled by default
     *
     * @see JDASlashCommand.defaultLocked
     * @see JDAUserCommand.defaultLocked
     * @see JDAMessageCommand.defaultLocked
     */
    var isDefaultLocked: Boolean
}
