package io.github.freya022.botcommands.internal.commands.application.mixins

import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandInfo
import io.github.freya022.botcommands.api.commands.application.TopLevelApplicationCommandMetadata
import io.github.freya022.botcommands.internal.commands.application.builder.ApplicationCommandBuilderImpl
import io.github.freya022.botcommands.internal.utils.requireAt

internal interface TopLevelApplicationCommandInfoMixin : TopLevelApplicationCommandInfo {
    override var metadata: TopLevelApplicationCommandMetadata

    fun initChecks(builder: ApplicationCommandBuilderImpl<*>) {
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