package doc.kotlin.examples.commands.slash

import dev.freya02.botcommands.jda.ktx.components.into
import dev.freya02.botcommands.jda.ktx.coroutines.await
import dev.freya02.botcommands.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.ComponentData
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.builder.bindWith
import io.github.freya022.botcommands.api.components.event.ButtonEvent

@Command
@RequiresComponents
class SlashTypeSafeButtons(private val buttons: Buttons) {
    @JDASlashCommand(name = "type_safe_buttons", description = "Demo of Kotlin type-safe bindings")
    suspend fun onSlashTypeSafeButtons(event: GuildSlashEvent, @SlashOption argument: String) {
        val button = buttons.primary("Click me").persistent {
            bindWith(::onTestClick, argument)
        }

        event.replyComponents(button.into()).await()
    }

    @JDAButtonListener
    suspend fun onTestClick(event: ButtonEvent, @ComponentData argument: String) {
        event.reply_("The argument was: $argument", ephemeral = true).await()
    }
}
