package io.github.freya022.botcommands.test.commands.text

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.components.Buttons
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.annotations.RequiresComponents
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import kotlinx.coroutines.delay
import kotlin.system.measureTimeMillis

@Command
@RequiresComponents
class TextDelay : TextCommand() {
    @JDATextCommandVariation(path = ["delay"])
    suspend fun runDelay(event: BaseCommandEvent, buttons: Buttons) {
        val millis = measureTimeMillis {
            delay(1000)
        }

        event.message.reply("delayed after $millis ms")
                .setActionRow(
                        buttons.primary("Delay").persistent {
                            bindTo("delayButton")
                        }
                )
                .queue()

        throw IllegalArgumentException()
    }

    @JDAButtonListener(name = "delayButton")
    suspend fun runDelayButton(event: ButtonEvent) {
        event.deferReply(true).queue()

        val millis = measureTimeMillis {
            delay(1000)
        }

        event.hook.sendMessage("delayed after $millis ms").setEphemeral(true).queue()
    }
}