package doc.kotlin.examples.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.Length
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.builder.bindTo
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.test.switches.TestLanguage
import net.dv8tion.jda.api.interactions.components.buttons.Button

@Command
@RequiresComponents
@TestLanguage(TestLanguage.Language.KOTLIN)
class SlashSayAgainPersistent : ApplicationCommand() {
    @TopLevelSlashCommandData
    @JDASlashCommand(name = "say_again", subcommand = "persistent", description = "Sends a button to send a message again")
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

        event.reply("This button always works")
            .addActionRow(persistentSaySentenceButton)
            .await()
    }

    @JDAButtonListener("SlashSayAgainPersistent: saySentenceButton")
    suspend fun onSaySentenceClick(event: ButtonEvent, sentence: String) {
        event.reply_(sentence, ephemeral = true).await()
    }
}
