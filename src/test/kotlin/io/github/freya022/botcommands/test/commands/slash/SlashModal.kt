package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.ModalData
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.api.modals.create
import io.github.freya022.botcommands.api.modals.shortTextInput
import io.github.freya022.botcommands.test.CustomObject
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import kotlin.time.Duration.Companion.seconds

private const val SLASH_MODAL_MODAL_HANDLER = "SlashModal: modalHandler"
private const val SLASH_MODAL_TEXT_INPUT = "SlashModal: textInput"

@Command
@Dependencies(Components::class)
class SlashModal(private val components: Components) : ApplicationCommand() {
    @JDASlashCommand(name = "modal_annotated")
    suspend fun onSlashModal(event: GuildSlashEvent, modals: Modals) {
        val modal = modals.create("Title") {
            shortTextInput(SLASH_MODAL_TEXT_INPUT, "Sample text")

            bindTo(SLASH_MODAL_MODAL_HANDLER, "User data", 420, null)

//            bindTo { event -> onModalSubmitted(event, "User data", 420, event.values[0].asString, CustomObject()) }

            timeout(5.seconds) {
                event.hook.send("Timeout !", ephemeral = true).queue()
            }
        }

        event.replyModal(modal).queue()

//        val modalEvent = modal.await()
//
//        onModalSubmitted(modalEvent, "User data", 420, modalEvent.values[0].asString, CustomObject())
    }

    @ModalHandler(name = SLASH_MODAL_MODAL_HANDLER)
    fun onModalSubmitted(
        event: ModalInteractionEvent,
        @ModalData dataStr: String,
        @ModalInput(name = SLASH_MODAL_TEXT_INPUT) inputStr: String,
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
            definitelyNull: $definitelyNull
            customObject: $customObject
            """.trimIndent(),
            components = listOf(row(components.ephemeralButton(ButtonStyle.PRIMARY, "Test button") {
                bindTo(::handleButton)
            })),
            ephemeral = true
        ).queue()
    }

    private fun handleButton(event: ButtonEvent) {
        event.deferEdit().queue()

        println(event.message.interaction?.user?.asMention)
    }

    @AppDeclaration
    fun declare(applicationCommandManager: GlobalApplicationCommandManager) {
        applicationCommandManager.slashCommand("modal", function = ::onSlashModal) {
            customOption("modals")
        }
    }
}