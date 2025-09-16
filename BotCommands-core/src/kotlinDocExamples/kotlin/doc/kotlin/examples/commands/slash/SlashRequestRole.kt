package doc.kotlin.examples.commands.slash

import dev.freya02.botcommands.jda.ktx.components.EntitySelectMenu
import dev.freya02.botcommands.jda.ktx.components.StringSelectMenu
import dev.freya02.botcommands.jda.ktx.components.TextInput
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.api.modals.create
import net.dv8tion.jda.api.components.selections.EntitySelectMenu.SelectTarget
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.entities.Role

private const val MODAL_NAME = "request role"
private const val INPUT_REASON = "reason"
private const val INPUT_ROLE = "role"
private const val INPUT_DETAILS = "details"

@Command
class SlashRequestRole(private val modals: Modals) {

    @JDASlashCommand(name = "request_role", description = "Request a role")
    fun onSlashRequestRole(event: GuildSlashEvent) {
        val modal = modals.create("Role Request Form") {
            text(
                """
                    ### Welcome!
                    Please read the following before continuing:
                    1. Select the role you wish to get
                    2. Select the reason why you want this role
                    3. (Optional) Add any detail about your request
                    
                    -# Abuse of this system may result in penalties
                """.trimIndent()
            )

            label("Role") {
                child = EntitySelectMenu(INPUT_ROLE, SelectTarget.ROLE)
            }

            label("Reason") {
                child = StringSelectMenu(INPUT_REASON) {
                    option("It looks cool!", "cool")
                    option("I like the color", "color")
                    option("I am interested in the relevant discussions", "discussions")
                }
            }

            label("Details") {
                child = TextInput(INPUT_DETAILS, TextInputStyle.PARAGRAPH, isRequired = false)
            }

            bindTo(MODAL_NAME)
        }

        event.replyModal(modal).queue()
    }

    @ModalHandler(MODAL_NAME)
    fun onRequestRoleModal(
        event: ModalEvent,
        @ModalInput(INPUT_REASON) reason: List<String>,
        @ModalInput(INPUT_ROLE) roles: List<Role>,
        @ModalInput(INPUT_DETAILS) details: String,
    ) {
        event.reply("Your request has been submitted!")
            .setEphemeral(true)
            .queue()
    }
}
