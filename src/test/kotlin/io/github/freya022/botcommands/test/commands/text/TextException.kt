package io.github.freya022.botcommands.test.commands.text

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.messages.into
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.modals.Modals
import io.github.freya022.botcommands.api.modals.create
import io.github.freya022.botcommands.api.modals.shortTextInput
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle

@Command
class TextException : TextCommand() {
    @JDATextCommandVariation(path = ["exception"])
    suspend fun onTextException(event: BaseCommandEvent, components: Components, modals: Modals) {
        event.context.dispatchException("test no throwable", null)
        event.context.dispatchException("test no throwable, with context", null, mapOf("pi" to 3.14159))

        event.channel.sendMessageComponents(
            components.ephemeralButton(ButtonStyle.DANGER, "Trigger modal and exception")
                .bindTo {
                    val modal = modals.create("Exception modal") {
                        shortTextInput("input name", "Sample text")

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