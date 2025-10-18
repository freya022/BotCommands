package dev.freya02.botcommands.bot.commands.slash

import dev.freya02.botcommands.bot.CustomObject
import dev.freya02.botcommands.jda.ktx.components.AttachmentUpload
import dev.freya02.botcommands.jda.ktx.components.EntitySelectMenu
import dev.freya02.botcommands.jda.ktx.components.StringSelectMenu
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
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import io.github.freya022.botcommands.api.modals.ModalEvent
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.ModalData
import io.github.freya022.botcommands.api.modals.annotations.ModalHandler
import io.github.freya022.botcommands.api.modals.annotations.ModalInput
import io.github.freya022.botcommands.api.modals.annotations.RequiresModals
import io.github.freya022.botcommands.api.modals.create
import net.dv8tion.jda.api.components.selections.EntitySelectMenu.SelectTarget
import net.dv8tion.jda.api.entities.IMentionable
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.interactions.IntegrationType
import kotlin.time.Duration.Companion.minutes

private const val SLASH_MODAL_MODAL_HANDLER = "SlashModal: modalHandler"
private const val SLASH_MODAL_TEXT_INPUT = "SlashModal: textInput"
private const val SLASH_MODAL_STRING_SELECT_INPUT = "SlashModal: stringSelect"
private const val SLASH_MODAL_ENTITY_SELECT_INPUT = "SlashModal: entitySelect"
private const val SLASH_MODAL_CHANNEL_SELECT_INPUT = "SlashModal: channelSelect"
private const val SLASH_MODAL_ATTACHMENT_INPUT = "SlashModal: attachment"

@Command
@RequiresModals
@RequiresComponents
class SlashModal(private val buttons: Buttons) : ApplicationCommand(), GlobalApplicationCommandProvider {
    @JDASlashCommand(name = "modal_annotated")
    suspend fun onSlashModal(event: GuildSlashEvent, modals: Modals) {
        val modal = modals.create("Title") {
            text("This is a text display")

//            label("Sample text") {
//                child = TextInput(SLASH_MODAL_TEXT_INPUT, TextInputStyle.SHORT)
//            }

            label("String select menu") {
                child = StringSelectMenu(SLASH_MODAL_STRING_SELECT_INPUT, required = false) {
                    option("Opt1", "opt1")
                    option("Opt2", "opt2", default = true)
                }
            }

            label("User and role select menu") {
                child = EntitySelectMenu(SLASH_MODAL_ENTITY_SELECT_INPUT, SelectTarget.USER, SelectTarget.ROLE, required = false)
            }

            label("Channel select menu") {
                child = EntitySelectMenu(
                    SLASH_MODAL_CHANNEL_SELECT_INPUT,
                    type = SelectTarget.CHANNEL,
                    channelTypes = enumSetOf(ChannelType.CATEGORY),
                    required = false
                )
            }

            label("Attachment") {
                child = AttachmentUpload(
                    SLASH_MODAL_ATTACHMENT_INPUT,
                    range = 1..2,
                    required = false
                )
            }

            bindTo(SLASH_MODAL_MODAL_HANDLER, "User data", 420, null)

//            bindTo { event -> onModalSubmitted(event, "User data", 420, event.values[0].asString, CustomObject()) }

            timeout(1.minutes) {
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
//        @ModalInput(customId = SLASH_MODAL_TEXT_INPUT) inputStr: String,
        @ModalInput(customId = SLASH_MODAL_STRING_SELECT_INPUT) selectedStrings: List<String>,
        @ModalInput(customId = SLASH_MODAL_ENTITY_SELECT_INPUT) selectedEntities: List<IMentionable>,
        @ModalInput(customId = SLASH_MODAL_CHANNEL_SELECT_INPUT) selectedChannels: List<GuildChannel>,
        @ModalInput(customId = SLASH_MODAL_ATTACHMENT_INPUT) attachments: List<Message.Attachment>,
        @ModalData dataInt: Int,
        @ModalData definitelyNull: Any?,
        customObject: CustomObject
    ) {
        event.reply_(
            """
            Submitted:
            dataStr: $dataStr
            dataInt: $dataInt
            selectedStrings: $selectedStrings
            selectedEntities: $selectedEntities
            selectedChannels: $selectedChannels
            attachments: $attachments
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
