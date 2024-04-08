package doc.kotlin.examples.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.Length
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.RequiresComponents
import io.github.freya022.botcommands.api.core.utils.after
import io.github.freya022.botcommands.test.switches.TestLanguage
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.utils.TimeFormat
import kotlin.time.Duration.Companion.seconds

@Command
@RequiresComponents
@TestLanguage(TestLanguage.Language.KOTLIN)
class SlashSayAgainEphemeral : ApplicationCommand() {
    @JDASlashCommand(name = "say_again", subcommand = "ephemeral", description = "Sends a button to send a message again")
    suspend fun onSlashSayAgain(
        event: GuildSlashEvent,
        @SlashOption @Length(max = Button.LABEL_MAX_LENGTH - 6) sentence: String,
        buttons: Buttons
    ) {
        // A button, which gets invalidated after restart, here it gets deleted after a timeout of 10 seconds
        // We have to use lateinit as the button is used in a callback
        lateinit var temporarySaySentenceButton: Button
        temporarySaySentenceButton = buttons.primary("Say '$sentence'").ephemeral {
            // Make sure only the caller can use the button
            constraints += event.user

            // The code to run when the button gets clicked
            bindTo { buttonEvent -> buttonEvent.reply_(sentence, ephemeral = true).await() }

            // Disables this button after 10 seconds
            timeout(10.seconds) {
                val newRow = row(temporarySaySentenceButton.asDisabled())
                event.hook.editOriginalComponents(newRow).await() // Coroutines!
            }
        }

        event.reply("This button expires ${TimeFormat.RELATIVE.after(10.seconds)}")
            .addActionRow(temporarySaySentenceButton)
            .await()
    }
}
