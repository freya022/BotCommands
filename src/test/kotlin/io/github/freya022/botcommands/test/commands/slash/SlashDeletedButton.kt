package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.messages.into
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Buttons

@Command
class SlashDeletedButton(private val buttons: Buttons) : ApplicationCommand() {
    @JDASlashCommand(name = "deleted_button")
    suspend fun onSlashDeletedButton(event: GuildSlashEvent) {
        val trap = buttons.danger("DO NOT SEND").ephemeral {}
        val deleteButton = buttons.primary("Delete").ephemeral {
            bindTo {
                it.deferEdit().queue()
                it.hook.deleteOriginal().queue()
                buttons.deleteComponentsById(listOf(trap.id))
            }
        }

        event.replyComponents(deleteButton.into()).queue()

        trap.await()

        // ?
    }
}