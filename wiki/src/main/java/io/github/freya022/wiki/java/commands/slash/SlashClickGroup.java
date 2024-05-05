package io.github.freya022.wiki.java.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.botcommands.api.components.event.ButtonEvent;
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.time.Duration;

@SuppressWarnings("CodeBlock2Expr")
@WikiCommandProfile(WikiCommandProfile.Profile.JAVA)
// --8<-- [start:click_group-java]
@Command
public class SlashClickGroup extends ApplicationCommand {
    // Since Java doesn't have the same method references as Kotlin,
    // we should use a constant name, so we don't have to type it more than once.
    private static final String COOKIE_BUTTON_NAME = "SlashPersistentClicker: cookie";

    private final Buttons buttons;

    public SlashClickGroup(Buttons buttons) {
        this.buttons = buttons;
    }

    @JDASlashCommand(name = "click_group", description = "Sends two buttons and waits for any of them to be clicked")
    public void onSlashClicker(GuildSlashEvent event) {
        final var firstButton = buttons.primary("1")
                .ephemeral()
                // Disable the timeout so we can use a group timeout
                .noTimeout()

                // Make it so this button is only usable once
                .oneUse(true)

                // Only allow the caller to use the button
                .constraints(interactionConstraints -> {
                    interactionConstraints.addUsers(event.getUser());
                })

                // Run this method when the button is clicked
                .bindTo(this::onButtonClick)
                .build();

        final var secondButton = buttons.primary("2")
                .ephemeral()
                // Disable the timeout so we can use a group timeout
                .noTimeout()

                // Make it so this button is only usable once
                .oneUse(true)

                // Only allow the caller to use the button
                .constraints(interactionConstraints -> {
                    interactionConstraints.addUsers(event.getUser());
                })

                // Run this method when the button is clicked
                .bindTo(this::onButtonClick)
                .build();
        // Construct our group, make it expire after 1 minute
        buttons.group(firstButton, secondButton)
                .ephemeral()
                .timeout(Duration.ofMinutes(1), () -> onButtonTimeout(event))
                .build();

        event.replyComponents(ActionRow.of(firstButton, secondButton)).queue();
    }

    private void onButtonClick(ButtonEvent event) {
        event.editButton(event.getButton().asDisabled()).queue();
        event.getHook().editOriginal("Try clicking the other button, you can't :^)").queue();
    }

    private void onButtonTimeout(GuildSlashEvent event) {
        event.getHook()
                .editOriginal("Expired!")
                .setReplace(true)
                .queue();
    }
}
// --8<-- [end:click_group-java]
