package io.github.freya022.botcommands.test_kt.commands.text

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.prefixed.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.prefixed.TextCommand
import io.github.freya022.botcommands.api.commands.prefixed.annotations.JDATextCommand
import io.github.freya022.botcommands.api.components.Components
import io.github.freya022.botcommands.api.components.annotations.JDAButtonListener
import io.github.freya022.botcommands.api.components.event.ButtonEvent
import io.github.freya022.botcommands.api.core.service.annotations.Dependencies
import kotlinx.coroutines.delay
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import kotlin.system.measureTimeMillis

@Command
@Dependencies(Components::class)
class TextDelay : TextCommand() {
    @JDATextCommand(name = "delay")
    suspend fun runDelay(event: BaseCommandEvent, components: Components) {
        val millis = measureTimeMillis {
            delay(1000)
        }

        event.message.reply("delayed after $millis ms")
                .setActionRow(
                        components.persistentButton(ButtonStyle.PRIMARY, "Delay") {
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