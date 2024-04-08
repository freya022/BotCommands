package doc.java.examples.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.Length;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.botcommands.api.components.RequiresComponents;
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener;
import io.github.freya022.botcommands.api.components.event.ButtonEvent;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

@Command
@RequiresComponents
@TestLanguage(TestLanguage.Language.JAVA)
public class SlashSayAgainPersistent extends ApplicationCommand {
    private static final String SAY_SENTENCE_HANDLER_NAME = "SlashSayAgainPersistent: saySentenceButton";

    @TopLevelSlashCommandData
    @JDASlashCommand(name = "say_again", subcommand = "persistent", description = "Sends a button to send a message again")
    public void onSlashSayAgain(
            GuildSlashEvent event,
            @SlashOption @Length(max = Button.LABEL_MAX_LENGTH - 6) String sentence,
            Buttons buttons
    ) {
        // A button that always works, even after a restart
        final var persistentSaySentenceButton = buttons.secondary("Say '" + sentence + "'").persistent()
                // Make sure only the caller can use the button
                .addUsers(event.getUser())
                // The method annotated with a JDAButtonListener of the same name will get called,
                // with the sentence as the argument
                .bindTo(SAY_SENTENCE_HANDLER_NAME, sentence)
                .build();

        event.reply("This button always works")
                .addActionRow(persistentSaySentenceButton)
                .queue();
    }

    @JDAButtonListener(SAY_SENTENCE_HANDLER_NAME)
    public void onSaySentenceClick(ButtonEvent event, String sentence) {
        event.reply(sentence).setEphemeral(true).queue();
    }
}
