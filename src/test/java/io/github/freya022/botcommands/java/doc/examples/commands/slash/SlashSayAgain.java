package io.github.freya022.botcommands.java.doc.examples.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.Length;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption;
import io.github.freya022.botcommands.api.components.Components;
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener;
import io.github.freya022.botcommands.api.components.event.ButtonEvent;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@Command
@TestLanguage(TestLanguage.Language.JAVA)
public class SlashSayAgain extends ApplicationCommand {
    private static final String SAY_SENTENCE_HANDLER_NAME = "SlashSayAgain: saySentenceButton";

    @JDASlashCommand(name = "say_again", description = "Sends a button to send a message again")
    public void onSlashSayAgain(
            GuildSlashEvent event,
            @SlashOption @Length(max = Button.LABEL_MAX_LENGTH - 6) String sentence,
            Components componentsService
    ) {
        // A button that always works, even after a restart
        final var persistentSaySentenceButton = componentsService.persistentButton(ButtonStyle.SECONDARY, "Say '" + sentence + "'")
                // Make sure only the caller can use the button
                .addUsers(event.getUser())
                // The method annotated with a JDAButtonListener of the same name will get called,
                // with the sentence as the argument
                .bindTo(SAY_SENTENCE_HANDLER_NAME, sentence)
                .build();

        // A button that gets deleted after restart, here it gets deleted after a timeout of 10 seconds
        AtomicReference<Button> temporaryButtonRef = new AtomicReference<>();
        final var temporarySaySentenceButton = componentsService.ephemeralButton(ButtonStyle.PRIMARY, "Say '" + sentence + "'")
                // Make sure only the caller can use the button
                .addUsers(event.getUser())
                // The code to run when the button gets clicked
                .bindTo(buttonEvent -> buttonEvent.reply(sentence).setEphemeral(true).queue())
                // Disables this button after 10 seconds
                .timeout(Duration.ofSeconds(10), () -> {
                    final var newRow = ActionRow.of(persistentSaySentenceButton, temporaryButtonRef.get().asDisabled());
                    event.getHook().editOriginalComponents(newRow).queue();
                })
                .build();
        temporaryButtonRef.set(temporarySaySentenceButton); // We have to do this to get the button in our timeout handler

        event.reply("The first button always works, and the second button gets disabled after 10 seconds")
                .addActionRow(persistentSaySentenceButton, temporarySaySentenceButton)
                .queue();
    }

    @JDAButtonListener(SAY_SENTENCE_HANDLER_NAME)
    public void onSaySentenceClick(ButtonEvent event, String sentence) {
        event.reply(sentence).setEphemeral(true).queue();
    }
}