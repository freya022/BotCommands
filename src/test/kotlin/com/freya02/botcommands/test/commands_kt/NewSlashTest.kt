package com.freya02.botcommands.test.commands_kt

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.annotations.api.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.slash.GuildSlashEvent
import dev.minn.jda.ktx.messages.reply_

@CommandMarker
class NewSlashTest : ApplicationCommand() {
    @JDASlashCommand(name = "test")
    fun onSlashTest(event: GuildSlashEvent) {
        event.reply_("woo", ephemeral = true).queue()
    }
}