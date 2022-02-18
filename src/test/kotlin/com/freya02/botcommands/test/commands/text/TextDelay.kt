package com.freya02.botcommands.test.commands.text

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.components.Components
import com.freya02.botcommands.api.components.annotations.JDAButtonListener
import com.freya02.botcommands.api.components.event.ButtonEvent
import com.freya02.botcommands.api.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.prefixed.TextCommand
import com.freya02.botcommands.api.prefixed.annotations.JDATextCommand
import kotlinx.coroutines.delay
import kotlin.system.measureTimeMillis

@CommandMarker
class TextDelay : TextCommand() {
    @JDATextCommand(name = "delay")
    suspend fun runDelay(event: BaseCommandEvent) {
        val millis = measureTimeMillis {
            delay(1000)
        }

        event.message.reply("delayed after $millis ms")
                .setActionRow(
                        Components.primaryButton("delayButton").build("Delay")
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