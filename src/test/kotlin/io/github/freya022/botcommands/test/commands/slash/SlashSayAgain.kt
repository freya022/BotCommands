package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.Length
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.builder.bindTo
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.test.switches.TestLanguage
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import kotlin.time.Duration.Companion.seconds

// -------------------------------------------- USED AS AN EXAMPLE --------------------------------------------
@Command
@TestLanguage(TestLanguage.Language.KOTLIN)
class SlashSayAgain : ApplicationCommand() {
    @JDASlashCommand(name = "say_again", description = "Sends a button to send a message again")
    suspend fun onSlashSayAgain(
        event: GuildSlashEvent,
        @SlashOption @Length(max = Button.LABEL_MAX_LENGTH - 6) sentence: String,
        buttons: Buttons
    ) {
        // A button that always works, even after a restart
        val persistentSaySentenceButton = buttons.secondary("Say '$sentence'").persistent {
            // Make sure only the caller can use the button
            constraints += event.user

            // In Kotlin, you can use callable references,
            // which enables you to use persistent callbacks in a type-safe manner
            bindTo(::onSaySentenceClick, sentence)
        }

        // A button that gets deleted after restart, here it gets deleted after a timeout of 10 seconds
        // We have to use lateinit as the button is used in a callback
        lateinit var temporarySaySentenceButton: Button
        temporarySaySentenceButton = buttons.primary("Say '$sentence'").ephemeral {
            // The code to run when the button gets clicked
            bindTo { buttonEvent -> buttonEvent.reply(sentence).setEphemeral(true).await() }

            // Disables this button after 10 seconds
            timeout(10.seconds) {
                val newRow = ActionRow.of(persistentSaySentenceButton, temporarySaySentenceButton.asDisabled())
                event.hook.editOriginalComponents(newRow).await() // Coroutines!
            }
        }

        event.reply("The first button always works, and the second button gets disabled after 10 seconds")
            .addActionRow(persistentSaySentenceButton, temporarySaySentenceButton)
            .queue()
    }

    @JDAButtonListener("SlashSayAgain: saySentenceButton")
    suspend fun onSaySentenceClick(event: ButtonEvent, sentence: String) {
        event.reply(sentence).setEphemeral(true).await()
    }
}
