package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.annotations.NSFW
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import dev.minn.jda.ktx.messages.reply_

@CommandMarker
class SlashNsfw : ApplicationCommand() {
    @com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand(name = "nsfw_annotated")
    @NSFW(dm = true, guild = true)
    fun onSlashNsfw(event: GuildSlashEvent) {
        event.reply_("ok", ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(globalApplicationCommandManager: com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager) {
        globalApplicationCommandManager.slashCommand("nsfw") {
            nsfw {
                allowInGuild = true
                allowInDMs = true
            }

            function = ::onSlashNsfw
        }
    }
}