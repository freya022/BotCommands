package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.LengthRange
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.Length
import dev.minn.jda.ktx.messages.reply_

@CommandMarker
class SlashLength : ApplicationCommand() {
    @JDASlashCommand(name = "length_annotated")
    fun onSlashLength(
        event: GuildSlashEvent,
        @AppOption @Length(min = 1, max = 5) string: String
    ) = event.reply_(string, ephemeral = true).queue()

    @AppDeclaration
    fun declare(applicationCommandManager: GlobalApplicationCommandManager) {
        applicationCommandManager.slashCommand("length", function = ::onSlashLength) {
            option("string") {
                lengthRange = LengthRange.of(1, 5)
            }
        }
    }
}