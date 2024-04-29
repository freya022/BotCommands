package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.row
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.awaitAnyOrNull
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.utils.awaitUnit
import io.github.freya022.botcommands.api.core.utils.replaceWith
import kotlin.time.Duration.Companion.minutes

// --8<-- [start:click_group-kotlin]
@Command
class SlashClickGroup(private val buttons: Buttons) : ApplicationCommand() {
    @JDASlashCommand(name = "click_group", description = "Sends two buttons and waits for any of them to be clicked")
    suspend fun onSlashClickGroup(event: GuildSlashEvent) {
        val firstButton = buttons.primary("1").ephemeral {
            // Disable the timeout so we can use a group timeout
            noTimeout()

            // Make it so this button is only usable once
            oneUse = true

            // Only allow the caller to use the button
            constraints += event.user
        }
        val secondButton = buttons.primary("2").ephemeral {
            // Disable the timeout so we can use a group timeout
            noTimeout()

            // Make it so this button is only usable once
            oneUse = true

            // Only allow the caller to use the button
            constraints += event.user
        }
        val group = buttons.group(firstButton, secondButton).ephemeral {
            timeout(1.minutes)
        }
        event.replyComponents(row(firstButton, secondButton)).await()

        // Wait for the allowed user to click one of the buttons
        val buttonEvent = group.awaitAnyOrNull<ButtonEvent>()
            ?: return event.hook
                .replaceWith("Expired!")
                .awaitUnit()

        // Disable the other button
        buttonEvent.editButton(buttonEvent.component.asDisabled()).await()
        buttonEvent.hook.editOriginal("Try clicking the other button, you can't :^)").await()
    }
}
// --8<-- [end:click_group-kotlin]