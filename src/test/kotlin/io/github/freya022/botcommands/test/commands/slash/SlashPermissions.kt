package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.BotPermissions
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.UserPermissions
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.declaration.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import net.dv8tion.jda.api.Permission

@Command
class SlashPermissions : ApplicationCommand() {
    @BotPermissions(Permission.MANAGE_EVENTS)
    @UserPermissions(Permission.MANAGE_SERVER, Permission.ADMINISTRATOR)
    @JDASlashCommand(name = "permissions_annotated")
    fun onSlashPermissions(event: GuildSlashEvent) {
        event.reply_("Granted", ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("permissions", scope = CommandScope.GLOBAL_NO_DM, function = ::onSlashPermissions) {
            botPermissions += Permission.MANAGE_EVENTS
            userPermissions = enumSetOf(Permission.MANAGE_SERVER, Permission.ADMINISTRATOR)
        }
    }
}