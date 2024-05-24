package io.github.freya022.wiki.java.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.components.Button;
import io.github.freya022.botcommands.api.components.Buttons;
import io.github.freya022.botcommands.api.components.annotations.ComponentData;
import io.github.freya022.botcommands.api.components.annotations.ComponentTimeoutHandler;
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener;
import io.github.freya022.botcommands.api.components.annotations.TimeoutData;
import io.github.freya022.botcommands.api.components.data.ComponentTimeoutData;
import io.github.freya022.botcommands.api.components.event.ButtonEvent;
import io.github.freya022.wiki.switches.wiki.WikiCommandProfile;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.time.Duration;

@SuppressWarnings("CodeBlock2Expr")
@WikiCommandProfile(WikiCommandProfile.Profile.JAVA)
// --8<-- [start:persistent-clicker-java]
@Command
public class SlashClickerPersistent extends ApplicationCommand {
    // Since Java doesn't have the same method references as Kotlin,
    // we should use a constant name, so we don't have to type it more than once.
    private static final String COOKIE_BUTTON_NAME = "SlashPersistentClicker: cookie";

    private final Buttons buttons;

    public SlashClickerPersistent(Buttons buttons) {
        this.buttons = buttons;
    }

    @JDASlashCommand(name = "clicker", subcommand = "persistent", description = "Creates a button you can infinitely click")
    public void onSlashClicker(GuildSlashEvent event) {
        final Button button = createButton(event, 0);
        event.replyComponents(ActionRow.of(button)).queue();
    }

    // The name should be unique,
    // I recommend naming the handler "[ClassName]: [purpose]"
    // And the name would be "on[purpose]Click"
    @JDAButtonListener(COOKIE_BUTTON_NAME)
    public void onCookieClick(ButtonEvent event, @ComponentData int count) {
        final Button newButton = createButton(event, count + 1);
        event.editComponents(ActionRow.of(newButton)).queue();
    }

    // Same thing here, names don't collide with other types of listener
    @ComponentTimeoutHandler(COOKIE_BUTTON_NAME)
    public void onCookieTimeout(ComponentTimeoutData timeout, @TimeoutData String count) {
        System.out.println("User finished clicking " + count + " cookies");
    }

    private Button createButton(Interaction event, int count) {
        // Create a primary-styled button
        return buttons.primary(count + " cookies")
                // Sets the emoji on the button,
                // this can be an unicode emoji, an alias or even a custom emoji
                .withEmoji("cookie")

                // Create a button that can be used even after a restart
                .persistent()

                // Make it so this button is only usable once
                // this is not an issue as we recreate the button everytime.
                // If this wasn't usable only once, the timeout would run for each button.
                .oneUse(true)

                // Only allow the caller to use the button
                .constraints(interactionConstraints -> {
                    interactionConstraints.addUsers(event.getUser());
                })

                // Timeout and call the method after the button hasn't been used for a day
                // The timeout gets cancelled if the button is invalidated
                .timeout(Duration.ofDays(1), COOKIE_BUTTON_NAME, count)

                // When clicked, run the onCookieClick method with the count
                .bindTo(COOKIE_BUTTON_NAME, count)
                .build();
    }
}
// --8<-- [end:persistent-clicker-java]
