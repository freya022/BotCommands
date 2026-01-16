package io.github.freya022.bot.commands.slash

import dev.freya02.botcommands.jda.ktx.components.StringSelectMenu
import dev.freya02.botcommands.jda.ktx.components.TextInput
import dev.freya02.botcommands.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.RequiresModals
import io.github.freya022.botcommands.api.modals.create
import net.dv8tion.jda.api.components.textinput.TextInputStyle

private const val codeInputId = "SlashModal: codeInput"
private const val languageInputId = "SlashModal: languageInput"

@Command
@RequiresModals
class SlashFormat(private val modals: Modals) {

    @JDASlashCommand(name = "format", description = "Formats your code")
    suspend fun onSlashFormat(event: GuildSlashEvent) {
        val modal = modals.create("Format your code") {
            label("Code") {
                child = TextInput(codeInputId, TextInputStyle.PARAGRAPH) {
                    minLength = 3
                }
            }

            label("Language") {
                child = StringSelectMenu(languageInputId) {
                    option("Kotlin", "kt")
                    option("Java", "java")
                }
            }
        }
        event.replyModal(modal).queue()

        val modalEvent = modal.await()
        val code = modalEvent.values[0].asString
        val language = modalEvent.values[1].asStringList[0]

        modalEvent.reply_(ephemeral = true) {
            content = """
                Here is your formatted code:
                ```$language
                $code```
            """.trimIndent()
        }.queue()
    }
}
