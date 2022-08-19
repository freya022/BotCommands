package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.annotations.api.annotations.UserPermissions
import com.freya02.botcommands.annotations.api.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.annotations.AppDeclaration
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.slash.GuildSlashEvent
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.Permission
import java.util.*

@CommandMarker
class SlashPermissions : ApplicationCommand() {
    @JDASlashCommand(scope = CommandScope.GLOBAL_NO_DM, name = "permissions_annotated")
    @UserPermissions(Permission.MANAGE_SERVER, Permission.ADMINISTRATOR)
    fun onSlashPermissions(event: GuildSlashEvent) {
        event.reply_("Granted", ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand(CommandPath.of("permissions"), scope = CommandScope.GLOBAL_NO_DM) {
            userPermissions = EnumSet.of(Permission.MANAGE_SERVER, Permission.ADMINISTRATOR)

            function = ::onSlashPermissions
        }
    }
}