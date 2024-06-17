package io.github.freya022.botcommands.internal.commands.application.mixins

import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.builder.ApplicationCommandBuilder
import io.github.freya022.botcommands.api.commands.application.slash.builder.mixins.ITopLevelApplicationCommandBuilder
import io.github.freya022.botcommands.internal.utils.downcast
import io.github.freya022.botcommands.internal.utils.requireAt
import io.github.freya022.botcommands.internal.utils.throwInternal

internal class TopLevelApplicationCommandInfoMixin internal constructor(
    builder: ITopLevelApplicationCommandBuilder
) : TopLevelApplicationCommandInfo {
    override val scope: CommandScope = builder.scope
    override val isDefaultLocked: Boolean = builder.isDefaultLocked
    override val isGuildOnly: Boolean = scope.isGuildOnly
    override val nsfw: Boolean = builder.nsfw

    override val metadata: Nothing get() = throwInternal("Should have been overridden by the implementation")

    init {
        downcast<ApplicationCommandBuilder<*>>(builder)

        //Administrators manage who can use what; the bot doesn't need to check for user mistakes
        // Why would you ask for a permission
        // if the administrators want a less-powerful user to be able to use it?
        if (isDefaultLocked) {
            requireAt(builder.userPermissions.isEmpty(), builder.declarationSite) {
                "Cannot put user permissions on default locked commands"
            }
        }

        if (builder.userPermissions.isNotEmpty() || builder.botPermissions.isNotEmpty()) {
            requireAt(isGuildOnly, builder.declarationSite) {
                "Application command with permissions should be guild-only, as permissions are not applicable in DMs"
            }
        }
    }
}