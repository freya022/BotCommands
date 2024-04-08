package doc.kotlin.examples.commands.slash

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.components.RequiresComponents
import io.github.freya022.botcommands.api.components.SelectMenus
import io.github.freya022.botcommands.api.components.annotations.JDASelectMenuListener
import io.github.freya022.botcommands.api.components.builder.bindTo
import io.github.freya022.botcommands.api.components.event.EntitySelectEvent
import io.github.freya022.botcommands.test.switches.TestLanguage
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget
import kotlin.random.Random

@Command
@RequiresComponents
@TestLanguage(TestLanguage.Language.KOTLIN)
class SlashSelectRolePersistent : ApplicationCommand() {
    @TopLevelSlashCommandData
    @JDASlashCommand(name = "select_role", subcommand = "persistent", description = "Sends a menu to choose a role from")
    suspend fun onSlashSelectRole(event: GuildSlashEvent, selectMenus: SelectMenus) {
        val randomNumber = Random.nextLong()
        val roleMenu = selectMenus.entitySelectMenu(SelectTarget.ROLE).persistent {
            // Make sure only the caller can use the button
            constraints += event.user

            // In Kotlin, you can use callable references,
            // which enables you to use persistent callbacks in a type-safe manner
            bindTo(::onRoleMenuSelect, randomNumber)
        }

        event.reply("This select menu always works")
            .addActionRow(roleMenu)
            .await()
    }

    @JDASelectMenuListener("SlashSelectRolePersistent: roleMenu")
    suspend fun onRoleMenuSelect(event: EntitySelectEvent, randomNumber: Long) {
        val role = event.values[0] as Role
        event.reply("You have been given " + role.asMention + ", and the random number is " + randomNumber)
            .setEphemeral(true)
            .await()
    }
}
