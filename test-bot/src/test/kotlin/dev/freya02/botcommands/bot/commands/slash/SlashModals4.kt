package dev.freya02.botcommands.bot.commands.slash

import dev.freya02.botcommands.jda.ktx.components.Checkbox
import dev.freya02.botcommands.jda.ktx.components.CheckboxGroup
import dev.freya02.botcommands.jda.ktx.components.RadioGroup
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.api.modals.create
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType

private const val MODAL_NAME = "SlashModals4: modal"
private const val CHECKBOX_ID = "checkbox-id"
private const val RADIO_GROUP_ID = "radio_group-id"
private const val CHECKBOX_GROUP_ID = "checkbox_group-id"

@Command
class SlashModals4(private val modals: Modals) {
    @TopLevelSlashCommandData(
        contexts = [InteractionContextType.GUILD],
        integrationTypes = [IntegrationType.USER_INSTALL]
    )
    @JDASlashCommand(name = "modals4")
    fun onSlashModals4(event: GuildSlashEvent) {
        val modal = modals.create("Modal") {
            label("I like checking boxes") {
                child = Checkbox(CHECKBOX_ID, isDefault = true)
            }

            label("Which Discord client do you use?") {
                child = RadioGroup(RADIO_GROUP_ID, required = false) {
                    option("Discord (Stable)", "stable", "The vanilla option", default = true)
                    option("Discord PTB", "ptb", "A peek into the future")
                    option("Discord Canary", "canary", "Living on the edge")
                }
            }

            label("Which modal components do you use?") {
                child = CheckboxGroup(CHECKBOX_GROUP_ID) {
                    option("Text Inputs", "textinputs")
                    option("Select Menus", "selectmenus")
                    option("File Uploads", "fileuploads")
                    option("Checkbox groups", "checkboxgroups", default = true)
                }
            }

            bindTo(MODAL_NAME)
        }

        event.replyModal(modal).queue()
    }

    @ModalHandler(MODAL_NAME)
    fun onModal(
        event: ModalEvent,
        @ModalInput(CHECKBOX_ID) doTheyLikeCheckingBoxes: Boolean,
        @ModalInput(RADIO_GROUP_ID) client: String = "<unspecified>",
        @ModalInput(CHECKBOX_GROUP_ID) features: List<String>,
    ) {
        event.reply("""
            Do you like checking boxes? $doTheyLikeCheckingBoxes
            On what client? $client
            Using what features? $features
        """.trimIndent())
            .setEphemeral(true)
            .queue()
    }
}
