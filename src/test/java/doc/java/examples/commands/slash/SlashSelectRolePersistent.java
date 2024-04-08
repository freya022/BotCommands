package doc.java.examples.commands.slash;

import io.github.freya022.botcommands.api.commands.annotations.Command;
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand;
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand;
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData;
import io.github.freya022.botcommands.api.components.EntitySelectMenu;
import io.github.freya022.botcommands.api.components.RequiresComponents;
import io.github.freya022.botcommands.api.components.SelectMenus;
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener;
import io.github.freya022.botcommands.api.components.event.EntitySelectEvent;
import io.github.freya022.botcommands.test.switches.TestLanguage;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget;

import java.util.concurrent.ThreadLocalRandom;

@Command
@RequiresComponents
@TestLanguage(TestLanguage.Language.JAVA)
public class SlashSelectRolePersistent extends ApplicationCommand {
    private static final String ROLE_MENU_HANDLER_NAME = "SlashSelectRolePersistent: roleMenu";

    @TopLevelSlashCommandData
    @JDASlashCommand(name = "select_role", subcommand = "persistent", description = "Sends a menu to choose a role from")
    public void onSlashSelectRole(
            GuildSlashEvent event,
            SelectMenus selectMenus
    ) {
        final long randomNumber = ThreadLocalRandom.current().nextLong();
        final EntitySelectMenu roleMenu = selectMenus.entitySelectMenu(SelectTarget.ROLE).persistent()
                // Make sure only the caller can use the select menu
                .addUsers(event.getUser())
                // The method annotated with a JDASelectMenuListener of the same name will get called,
                // with the random number as the argument
                .bindTo(ROLE_MENU_HANDLER_NAME, randomNumber)
                .build();

        event.reply("This select menu always works")
                .addActionRow(roleMenu)
                .queue();
    }

    @JDASelectMenuListener(ROLE_MENU_HANDLER_NAME)
    public void onRoleMenuSelect(EntitySelectEvent event, long randomNumber) {
        final Role role = (Role) event.getValues().get(0);
        event.reply("You have been given " + role.getAsMention() + ", and the random number is " + randomNumber)
                .setEphemeral(true)
                .queue();
    }
}
