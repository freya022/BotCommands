package io.github.freya022.wiki.java.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.components.Button;
import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.time.Duration;

@SuppressWarnings("CodeBlock2Expr")
@WikiCommandProfile(WikiCommandProfile.Profile.JAVA)
// --8<-- [start:ephemeral-clicker-java]
@Command
public class SlashClickerEphemeral extends ApplicationCommand {
    private final Buttons buttons;

    public SlashClickerEphemeral(Buttons buttons) {
        this.buttons = buttons;
    }

    @JDASlashCommand(name = "clicker", subcommand = "ephemeral", description = "Creates a button you can click until the bot restarts")
    public void onSlashClicker(GuildSlashEvent event) {
        final Button button = createButton(event, 0);
        event.replyComponents(ActionRow.of(button)).queue();
    }

    private Button createButton(Interaction event, int count) {
        // Create a primary-styled button
        return buttons.primary(count + " cookies")
                // Sets the emoji on the button,
                // this can be an unicode emoji, an alias or even a custom emoji
                .withEmoji("cookie")

                // Create a button that can be used until the bot restarts
                .ephemeral()

                // Make it so this button is only usable once
                // this is not an issue as we recreate the button everytime.
                // If this wasn't usable only once, the timeout would run for each button.
                .oneUse(true)

                // Only allow the caller to use the button
                .constraints(interactionConstraints -> {
                    interactionConstraints.addUsers(event.getUser());
                })

                // Run this callback after the button hasn't been used for a day
                // The timeout gets cancelled if the button is invalidated
                .timeout(Duration.ofDays(1), () -> {
                    System.out.println("User finished clicking " + count + " cookies");
                })

                // When clicked, run this callback
                .bindTo(buttonEvent -> {
                    final Button newButton = createButton(buttonEvent, count + 1);
                    buttonEvent.editComponents(ActionRow.of(newButton)).queue();
                })
                .build();
    }
}
// --8<-- [end:ephemeral-clicker-java]
