package dev.freya02.botcommands.bot.commands.text

import dev.freya02.botcommands.jda.ktx.components.TextInput
import dev.freya02.botcommands.jda.ktx.components.into
import dev.freya02.botcommands.jda.ktx.coroutines.await
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.annotations.RequiresModals
import io.github.freya022.botcommands.api.modals.create
import net.dv8tion.jda.api.components.textinput.TextInputStyle

@Command
@RequiresModals
@RequiresComponents
class TextException : TextCommand() {
    @JDATextCommandVariation(path = ["exception"])
    suspend fun onTextException(event: BaseCommandEvent, buttons: Buttons, modals: Modals) {
        event.context.dispatchException("test no throwable", null)
        event.context.dispatchException("test no throwable, with context", null, mapOf("pi" to 3.14159))

        event.channel.sendMessageComponents(
            buttons.danger("Trigger modal and exception").ephemeral()
                .bindTo {
                    val modal = modals.create("Exception modal") {
                        label("Sample text") {
                            child = TextInput("input name", TextInputStyle.SHORT)
                        }

                        bindTo {
                            throw RuntimeException("Modal exception")
                        }
                    }

                    it.replyModal(modal).await()

                    throw RuntimeException("Button exception")
                }
                .build()
                .into()
        ).await()

        throw RuntimeException("test throwable")
    }
}
