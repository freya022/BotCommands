package io.github.freya022.botcommands.test.commands.slash

import dev.freya02.botcommands.jda.ktx.components.StringSelectMenu
import dev.freya02.botcommands.jda.ktx.components.TextInput
import dev.freya02.botcommands.jda.ktx.components.row
import dev.freya02.botcommands.jda.ktx.messages.reply_
import dev.freya02.botcommands.jda.ktx.messages.send
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.provider.GlobalApplicationCommandProvider
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.ModalData
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.api.modals.annotations.RequiresModals
import io.github.freya022.botcommands.api.modals.create
import io.github.freya022.botcommands.test.CustomObject
import net.dv8tion.jda.api.components.textinput.TextInputStyle
import net.dv8tion.jda.api.interactions.IntegrationType
import kotlin.time.Duration.Companion.seconds

private const val SLASH_MODAL_MODAL_HANDLER = "SlashModal: modalHandler"
private const val SLASH_MODAL_TEXT_INPUT = "SlashModal: textInput"
private const val SLASH_MODAL_STRING_SELECT_INPUT = "SlashModal: stringSelect"

@Command
@RequiresModals
@RequiresComponents
class SlashModal(private val buttons: Buttons) : ApplicationCommand(), GlobalApplicationCommandProvider {
    @JDASlashCommand(name = "modal_annotated")
    suspend fun onSlashModal(event: GuildSlashEvent, modals: Modals) {
        val modal = modals.create("Title") {
            label("Sample text") {
                child = TextInput(SLASH_MODAL_TEXT_INPUT, TextInputStyle.SHORT)
            }

            label("Select menu") {
                child = StringSelectMenu(SLASH_MODAL_STRING_SELECT_INPUT) {
                    option("Opt1", "opt1")
                    option("Opt2", "opt2", default = true)
                }
            }

            bindTo(SLASH_MODAL_MODAL_HANDLER, "User data", 420, null)

//            bindTo { event -> onModalSubmitted(event, "User data", 420, event.values[0].asString, CustomObject()) }

            timeout(15.seconds) {
                event.hook.send("Timeout !", ephemeral = true).queue()
            }
        }

        event.replyModal(modal).queue()

//        val modalEvent = modal.await()
//
//        onModalSubmitted(modalEvent, "User data", 420, modalEvent.values[0].asString, CustomObject())
    }

    @ModalHandler(name = SLASH_MODAL_MODAL_HANDLER)
    suspend fun onModalSubmitted(
        event: ModalEvent,
        @ModalData dataStr: String,
        @ModalInput(customId = SLASH_MODAL_TEXT_INPUT) inputStr: String,
        @ModalInput(customId = SLASH_MODAL_STRING_SELECT_INPUT) selectedStrings: List<String>,
        @ModalData dataInt: Int,
        @ModalData definitelyNull: Any?,
        customObject: CustomObject
    ) {
        event.reply_(
            """
            Submitted:
            dataStr: $dataStr
            dataInt: $dataInt
            inputStr: $inputStr
            selectedStrings: $selectedStrings
            definitelyNull: $definitelyNull
            customObject: $customObject
            """.trimIndent(),
            components = listOf(row(buttons.primary("Test button").ephemeral {
                bindTo(::handleButton)
            })),
            ephemeral = true
        ).queue()
    }

    private fun handleButton(event: ButtonEvent) {
        event.deferEdit().queue()

        println(event.message.interactionMetadata?.user?.asMention)
    }

    override fun declareGlobalApplicationCommands(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("modal", function = ::onSlashModal) {
            integrationTypes = IntegrationType.ALL
            serviceOption("modals")
        }
    }
}
