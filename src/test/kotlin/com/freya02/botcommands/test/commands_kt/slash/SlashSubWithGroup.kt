package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.slash.GuildSlashEvent
import dev.minn.jda.ktx.messages.reply_

@CommandMarker
class SlashSubWithGroup : ApplicationCommand() {
    @CommandMarker
    fun onSlashSubWithGroup(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    @Declaration
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