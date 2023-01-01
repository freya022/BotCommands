package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.modals.Modals
import com.freya02.botcommands.api.modals.annotations.ModalData
import com.freya02.botcommands.api.modals.annotations.ModalHandler
import com.freya02.botcommands.api.modals.annotations.ModalInput
import com.freya02.botcommands.api.modals.create
import com.freya02.botcommands.api.modals.shortTextInput
import com.freya02.botcommands.test_kt.CustomObject
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import dev.minn.jda.ktx.messages.send
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.util.concurrent.TimeUnit

private const val SLASH_MODAL_MODAL_HANDLER = "SlashModal: modalHandler"
private const val SLASH_MODAL_TEXT_INPUT = "SlashModal: textInput"

@CommandMarker
class SlashModal(private val components: Components) : ApplicationCommand() {
    @JDASlashCommand(name = "modal_annotated")
    suspend fun onSlashModal(event: GuildSlashEvent, modals: Modals) {
        val modal = modals.create("Title") {
            shortTextInput(SLASH_MODAL_TEXT_INPUT, "Sample text")

//            bindTo(SLASH_MODAL_MODAL_HANDLER, "User data", 420)

//            bindTo { event -> onModalSubmitted(event, "User data", 420, event.values[0].asString, CustomObject()) }

            setTimeout(5, TimeUnit.SECONDS) {
                event.hook.send("Timeout !", ephemeral = true).queue()
            }
        }

        event.replyModal(modal).queue()

        val modalEvent = modal.await()

        onModalSubmitted(modalEvent, "User data", 420, modalEvent.values[0].asString, CustomObject())
    }

    @ModalHandler(name = SLASH_MODAL_MODAL_HANDLER)
    fun onModalSubmitted(
        event: ModalInteractionEvent,
        @ModalData dataStr: String,
        @ModalData dataInt: Int,
        @ModalInput(name = SLASH_MODAL_TEXT_INPUT) inputStr: String,
        customObject: CustomObject
    ) {
        event.reply_(
            """
            Submitted:
            dataStr: $dataStr
            dataInt: $dataInt
            inputStr: $inputStr
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
        applicationCommandManager.slashCommand("modal") {
            customOption("modals")

            function = ::onSlashModal
        }
    }
}