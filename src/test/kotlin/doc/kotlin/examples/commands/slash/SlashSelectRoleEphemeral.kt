package doc.kotlin.examples.commands.slash

import dev.minn.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.EntitySelectMenu
import io.github.freya022.botcommands.api.components.SelectMenus
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.core.utils.after
import io.github.freya022.botcommands.test.switches.TestLanguage
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.selections.EntitySelectMenu.SelectTarget
import net.dv8tion.jda.api.utils.TimeFormat
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

@Command
@RequiresComponents
@TestLanguage(TestLanguage.Language.KOTLIN)
class SlashSelectRoleEphemeral : ApplicationCommand() {
    @JDASlashCommand(name = "select_role", subcommand = "ephemeral", description = "Sends a menu to choose a role from")
    suspend fun onSlashSelectRole(event: GuildSlashEvent, selectMenus: SelectMenus) {
        val randomNumber = Random.nextLong()

        // A select menu, which gets invalidated after restart, here it gets deleted after a timeout of 10 seconds
        lateinit var temporarySelectMenu: EntitySelectMenu
        val roleMenu = selectMenus.entitySelectMenu(SelectTarget.ROLE).ephemeral {
            // Make sure only the caller can use the select menu
            constraints += event.user

            bindTo { selectEvent ->
                val role = selectEvent.values.first() as Role
                selectEvent.reply("You have been given " + role.asMention + ", and the random number is " + randomNumber)
                    .setEphemeral(true)
                    .await()
            }

            // Disables this button after 10 seconds
            timeout(10.seconds) {
                val newRow = ActionRow.of(temporarySelectMenu.asDisabled())
                event.hook.editOriginalComponents(newRow).await() // Coroutines!
            }
        }
        temporarySelectMenu = roleMenu

        event.reply("This select menu expires ${TimeFormat.RELATIVE.after(10.seconds)}")
            .addActionRow(roleMenu)
            .await()
    }
}
