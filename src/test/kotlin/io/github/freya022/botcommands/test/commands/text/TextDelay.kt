package io.github.freya022.botcommands.test.commands.text

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import kotlinx.coroutines.delay
import kotlin.system.measureTimeMillis

@Command
@Dependencies(Components::class)
class TextDelay : TextCommand() {
    @JDATextCommandVariation(path = ["delay"])
    suspend fun runDelay(event: BaseCommandEvent, components: Components) {
        val millis = measureTimeMillis {
            delay(1000)
        }

        event.message.reply("delayed after $millis ms")
                .setActionRow(
                        components.primaryButton("Delay").persistent {
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