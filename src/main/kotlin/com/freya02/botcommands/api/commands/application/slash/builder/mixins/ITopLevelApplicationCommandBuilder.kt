package com.freya02.botcommands.api.commands.application.slash.builder.mixins

import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import com.freya02.botcommands.api.commands.application.context.annotations.JDAUserCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand

interface ITopLevelApplicationCommandBuilder {
    /**
     * **Annotation equivalents:**
     * - [For slash commands][JDASlashCommand.scope]
     * - [For user context commands][JDAUserCommand.scope]
     * - [For message context commands][JDAMessageCommand.scope]
     *
     * @see JDASlashCommand.scope
     * @see JDAUserCommand.scope
     * @see JDAMessageCommand.scope
     */
    val scope: CommandScope

    /**
     * **Annotation equivalents:**
     * - [For slash commands][JDASlashCommand.defaultLocked]
     * - [For user context commands][JDAUserCommand.defaultLocked]
     * - [For message context commands][JDAMessageCommand.defaultLocked]
     *
     * @see JDASlashCommand.defaultLocked
     * @see JDAUserCommand.defaultLocked
     * @see JDAMessageCommand.defaultLocked
     */
    var isDefaultLocked: Boolean
}
