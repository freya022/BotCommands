package doc.java.examples.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.Length;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@Command
@RequiresComponents
@TestLanguage(TestLanguage.Language.JAVA)
public class SlashSayAgainEphemeral extends ApplicationCommand {
    @JDASlashCommand(name = "say_again", subcommand = "ephemeral", description = "Sends a button to send a message again")
    public void onSlashSayAgain(
            GuildSlashEvent event,
            @SlashOption @Length(max = Button.LABEL_MAX_LENGTH - 6) String sentence,
            Buttons buttons
    ) {
        // A button, which gets invalidated after restart, here it gets deleted after a timeout of 10 seconds
        AtomicReference<Button> temporaryButtonRef = new AtomicReference<>();
        final var temporarySaySentenceButton = buttons.primary("Say '" + sentence + "'").ephemeral()
                // Make sure only the caller can use the button
                .addUsers(event.getUser())
                // The code to run when the button gets clicked
                .bindTo(buttonEvent -> buttonEvent.reply(sentence).setEphemeral(true).queue())
                // Disables this button after 10 seconds
                .timeout(Duration.ofSeconds(10), () -> {
                    final var newRow = ActionRow.of(temporaryButtonRef.get().asDisabled());
                    event.getHook().editOriginalComponents(newRow).queue();
                })
                .build();
        temporaryButtonRef.set(temporarySaySentenceButton); // We have to do this to get the button in our timeout handler

        event.reply("This button expires " + TimeFormat.RELATIVE.after(Duration.ofSeconds(10)))
                .addActionRow(temporarySaySentenceButton)
                .queue();
    }
}
