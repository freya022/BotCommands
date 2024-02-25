package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.LengthRange
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.declaration.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.Length
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption

@Command
class SlashLength : ApplicationCommand() {
    @JDASlashCommand(name = "length_annotated")
    fun onSlashLength(
        event: GuildSlashEvent,
        @SlashOption @Length(min = 1, max = 5) string: String
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