package com.freya02.botcommands.internal.commands.application.mixins

import com.freya02.botcommands.api.commands.CooldownScope
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import com.freya02.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import com.freya02.botcommands.internal.utils.throwMixin
import com.freya02.botcommands.internal.utils.throwUser

open class TopLevelApplicationCommandInfoMixin(
    builder: ITopLevelApplicationCommandBuilder
) : ITopLevelApplicationCommandInfo {
    final override val scope: CommandScope = builder.scope
    final override val isDefaultLocked: Boolean = builder.isDefaultLocked
    final override val isGuildOnly: Boolean = scope.isGuildOnly

    init {
        builder as? ApplicationCommandBuilder<*> ?: throwMixin<ApplicationCommandBuilder<*>>()

        if (builder.cooldownStrategy.scope != CooldownScope.USER && !scope.isGuildOnly) {
            throwUser("Application command cannot have a ${builder.cooldownStrategy.scope} cooldown scope with a global slash command")
        }

        if (isTestOnly && scope != CommandScope.GUILD) {
            throwUser("Application command annotated with @Test must have the GUILD scope")
        }

        //Administrators manage who can use what, bot doesn't need to check for user mistakes
        // Why would you ask for a permission if the administrators want a less-powerful user to be able to use it ?
        if (isDefaultLocked && builder.userPermissions.isNotEmpty()) {
            throwUser(builder.function, "Cannot put user permissions on default locked commands")
        }

        if (!isGuildOnly && (builder.userPermissions.isNotEmpty() || builder.botPermissions.isNotEmpty())) {
            throwUser("Application command with permissions should be guild-only, as permissions are not applied in DMs")
        }
    }
}