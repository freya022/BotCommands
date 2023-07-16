package com.freya02.botcommands.api.commands.application.slash.builder.mixins

import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import com.freya02.botcommands.api.commands.application.context.annotations.JDAUserCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand

interface ITopLevelApplicationCommandBuilder {
    /**
     * @see JDASlashCommand.scope
     * @see JDAUserCommand.scope
     * @see JDAMessageCommand.scope
     */
    val scope: CommandScope

    /**
     * @see JDASlashCommand.defaultLocked
     * @see JDAUserCommand.defaultLocked
     * @see JDAMessageCommand.defaultLocked
     */
    var isDefaultLocked: Boolean
}
