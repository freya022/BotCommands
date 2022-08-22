package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.modals.Modals
import com.freya02.botcommands.api.modals.annotations.ModalData
import com.freya02.botcommands.api.modals.annotations.ModalHandler
import com.freya02.botcommands.api.modals.annotations.ModalInput
import com.freya02.botcommands.test.CustomObject
import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import java.util.concurrent.TimeUnit

private const val SLASH_MODAL_MODAL_HANDLER = "SlashModal: modalHandler"
private const val SLASH_MODAL_TEXT_INPUT = "SlashModal: textInput"

@CommandMarker
class SlashModal(private val components: Components) : ApplicationCommand() {
    @JDASlashCommand(name = "modal_annotated")
    fun onSlashModal(event: GuildSlashEvent, modals: Modals) {
        val input = modals.createTextInput(SLASH_MODAL_TEXT_INPUT, "Sample text", TextInputStyle.SHORT)
            .build()

        val modal = modals.create("Title", SLASH_MODAL_MODAL_HANDLER, "User data", 420)
            .setTimeout(30, TimeUnit.SECONDS) {
                println("Timeout")
            }
            .addActionRow(input)
            .build()

        event.replyModal(modal).queue()
    }

    @ModalHandler(name = SLASH_MODAL_MODAL_HANDLER)
    fun onModalSubmitted(
        event: ModalInteractionEvent,
        @ModalData dataStr: String,
        @ModalData dataInt: Int,
        @ModalInput(name = SLASH_MODAL_TEXT_INPUT) inputStr: String,
        customObject: CustomObject
    ) {
        event.reply_("""
            Submitted:
            dataStr: $dataStr
            dataInt: $dataInt
            inputStr: $inputStr
            customObject: $customObject
            """.trimIndent(),
            components = listOf(row(components.primaryButton(this::handleButton).build("Test button"))),
            ephemeral = true).queue()
    }

    private fun handleButton(event: ButtonEvent) {
        event.deferEdit().queue()

        println(event.message.interaction?.user?.asMention)
    }

    @AppDeclaration
    fun declare(applicationCommandManager: GuildApplicationCommandManager) {
        applicationCommandManager.slashCommand("modal", scope = CommandScope.GUILD) {
            customOption("modals")

            function = ::onSlashModal
        }
    }
}