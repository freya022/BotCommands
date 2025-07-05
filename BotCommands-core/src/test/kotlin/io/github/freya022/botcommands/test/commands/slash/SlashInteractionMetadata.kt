package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.interactions.components.row
import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAMessageCommand
import io.github.freya022.botcommands.api.commands.application.context.annotations.JDAUserCommand
import io.github.freya022.botcommands.api.commands.application.context.message.GuildMessageEvent
import io.github.freya022.botcommands.api.commands.application.context.user.GuildUserEvent
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.create
import io.github.freya022.botcommands.api.modals.shortTextInput
import net.dv8tion.jda.api.interactions.InteractionHook
import kotlin.time.Duration.Companion.minutes

@Command
class SlashInteractionMetadata(
    private val buttons: Buttons,
    private val modals: Modals,
) : ApplicationCommand() {

    @JDASlashCommand("interaction_metadata")
    suspend fun onSlashInteractionMetadata(event: GuildSlashEvent) {
        val modalButton = buttons.primary("Open modal").ephemeral {
            singleUse = true
            timeout(1.minutes)
        }
        val messageButton = buttons.secondary("Reply + followup").ephemeral {
            bindTo {
                it.reply_("Message from button", ephemeral = true).queue()
                it.hook.sendMessage("Followup from button").queue()
            }
        }
        event.reply_("Message from slash command", components = listOf(row(modalButton, messageButton)), ephemeral = true).queue()

        val buttonEvent = modalButton.await()
        val modal = modals.create("Interaction metadata") {
            timeout(1.minutes)

            shortTextInput("input name", "Text") {
                value = "Sample text"
            }
        }
        buttonEvent.replyModal(modal).queue()

        val modalEvent = modal.await()
        modalEvent.reply_("Message from modal", ephemeral = true).queue()
    }

    @JDAMessageCommand(name = "Interaction metadata")
    fun onMessageInteractionMetadata(event: GuildMessageEvent) {
        event.reply_("Message from message interaction", ephemeral = false)
            .flatMap(InteractionHook::retrieveOriginal)
            .queue()
    }

    @JDAUserCommand(name = "Interaction metadata")
    fun onUserInteractionMetadata(event: GuildUserEvent) {
        event.reply_("Message from user interaction", ephemeral = false).queue()
    }
}