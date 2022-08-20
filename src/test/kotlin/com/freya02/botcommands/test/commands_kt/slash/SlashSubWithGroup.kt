package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import dev.minn.jda.ktx.messages.reply_

@CommandMarker
class SlashSubWithGroup : ApplicationCommand() {
    @JDASlashCommand(name = "tag_annotated", subcommand = "send")
    fun onSlashSubWithGroup(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    @JDASlashCommand(name = "tag_annotated", group = "manage", subcommand = "create")
    fun onSlashSubWithGroupManageCreate(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    @JDASlashCommand(name = "tag_annotated", group = "manage", subcommand = "edit")
    fun onSlashSubWithGroupManageEdit(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.slashCommand("tag", subcommand = "send") {
            function = ::onSlashSubWithGroup
        }

        globalApplicationCommandManager.slashCommand("tag", group = "manage", subcommand = "create") {
            function = ::onSlashSubWithGroup
        }

        globalApplicationCommandManager.slashCommand("tag", group = "manage", subcommand = "edit") {
            function = ::onSlashSubWithGroup
        }
    }
}