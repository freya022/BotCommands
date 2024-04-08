package doc.java.examples.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.components.EntitySelectMenu;
import io.github.freya022.botcommands.api.components.RequiresComponents;
import io.github.freya022.botcommands.api.components.SelectMenus;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget;
import net.dv8tion.jda.api.utils.TimeFormat;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;

@Command
@RequiresComponents
@TestLanguage(TestLanguage.Language.JAVA)
public class SlashSelectRoleEphemeral extends ApplicationCommand {
    @JDASlashCommand(name = "select_role", subcommand = "ephemeral", description = "Sends a menu to choose a role from")
    public void onSlashSelectRole(
            GuildSlashEvent event,
            SelectMenus selectMenus
    ) {
        final long randomNumber = ThreadLocalRandom.current().nextLong();

        // A select menu, which gets invalidated after restart, here it gets deleted after a timeout of 10 seconds
        AtomicReference<EntitySelectMenu> temporarySelectMenuRef = new AtomicReference<>();
        final EntitySelectMenu roleMenu = selectMenus.entitySelectMenu(SelectTarget.ROLE).ephemeral()
                // Make sure only the caller can use the select menu
                .addUsers(event.getUser())
                // The code to run when the select menu is used
                .bindTo(selectEvent -> {
                    final Role role = (Role) selectEvent.getValues().get(0);
                    selectEvent.reply("You have been given " + role.getAsMention() + ", and the random number is " + randomNumber)
                            .setEphemeral(true)
                            .queue();
                })
                // Disables this button after 10 seconds
                .timeout(Duration.ofSeconds(10), () -> {
                    final var newRow = ActionRow.of(temporarySelectMenuRef.get().asDisabled());
                    event.getHook().editOriginalComponents(newRow).queue();
                })
                .build();
        temporarySelectMenuRef.set(roleMenu);

        event.reply("This select menu expires " + TimeFormat.RELATIVE.after(Duration.ofSeconds(10)))
                .addActionRow(roleMenu)
                .queue();
    }
}
