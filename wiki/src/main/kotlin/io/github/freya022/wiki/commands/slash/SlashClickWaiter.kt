package io.github.freya022.wiki.commands.slash

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.row
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.components.Button
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.utils.awaitUnit
import io.github.freya022.botcommands.api.core.utils.deleteDelayed
import io.github.freya022.botcommands.api.core.utils.replaceWith
import kotlinx.coroutines.TimeoutCancellationException
import kotlin.time.Duration.Companion.seconds

private suspend fun Button.awaitOrNull(): ButtonEvent? = try {
    await()
} catch (e: TimeoutCancellationException) {
    null
}

// --8<-- [start:click_waiter-kotlin]
@Command
class SlashClickWaiter(private val buttons: Buttons) : ApplicationCommand() {
    @JDASlashCommand(name = "click_waiter", description = "Sends a button and waits for it to be clicked")
    suspend fun onSlashClickWaiter(event: GuildSlashEvent) {
        val button = buttons.primary("Click me").ephemeral {
            // Make it so this button is only usable once
            oneUse = true

            // Only allow the caller to use the button
            constraints += event.user
        }
        event.replyComponents(row(button)).await()

        // Wait for the allowed user to click the button
        val buttonEvent: ButtonEvent = button.awaitOrNull() // (1)!
            ?: return event.hook
                .replaceWith("Expired!")
                .awaitUnit() // (2)!

        buttonEvent.editMessage("!")
            // Replace the entire message
            .setReplace(true)
            // Delete after 5 seconds
            .deleteDelayed(5.seconds)
            .await()
    }
}
// --8<-- [end:click_waiter-kotlin]