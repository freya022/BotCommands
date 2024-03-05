package io.github.freya022.bot.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.create
import io.github.freya022.botcommands.api.modals.paragraphTextInput

private const val codeInputName = "SlashModal: codeInput"

@Command
class SlashModal(private val modals: Modals) : ApplicationCommand() {
    @JDASlashCommand(name = "format", description = "Formats your code")
    suspend fun onSlashFormat(event: GuildSlashEvent) {
        val modal = modals.create("Format your code") {
            paragraphTextInput(codeInputName, "Code") {
                minLength = 3
            }
        }
        event.replyModal(modal).queue()

        val modalEvent = modal.await()
        val code = modalEvent.values.first().asString

        modalEvent.reply_(
            """
                Here is your formatted code:
                ```kt
                $code```
            """.trimIndent(), ephemeral = true).queue()
    }
}