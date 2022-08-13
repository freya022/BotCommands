package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.annotations.api.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.slash.GuildSlashEvent
import dev.minn.jda.ktx.messages.reply_

@CommandMarker
class SlashNsfw : ApplicationCommand() {
    @JDASlashCommand(name = "nsfw_annotated")
    fun onSlashNsfw(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    @Declaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.slashCommand("nsfw") {
            nsfw {
                allowInGuild = true
                allowInDMs = true
            }

            function = ::onSlashNsfw
        }
    }
}