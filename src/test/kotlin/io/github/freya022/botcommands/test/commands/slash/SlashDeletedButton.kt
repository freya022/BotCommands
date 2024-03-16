package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.messages.into
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.Components

@Command
class SlashDeletedButton(private val components: Components, private val buttons: Buttons) : ApplicationCommand() {
    @JDASlashCommand(name = "deleted_button")
    suspend fun onSlashDeletedButton(event: GuildSlashEvent) {
        val trap = buttons.dangerButton("DO NOT SEND").ephemeral {}
        val deleteButton = buttons.primaryButton("Delete").ephemeral {
            bindTo {
                it.deferEdit().queue()
                it.hook.deleteOriginal().queue()
                components.deleteComponentsById(listOf(trap.id))
            }
        }

        event.replyComponents(deleteButton.into()).queue()

        trap.await()

        // ?
    }
}