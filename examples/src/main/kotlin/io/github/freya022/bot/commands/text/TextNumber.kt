package io.github.freya022.bot.commands.text

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.TextOption
import kotlin.math.PI

@Command
class TextNumber : TextCommand() {
    @JDATextCommand(path = ["number"])
    fun onTextNumber(event: BaseCommandEvent, @TextOption number: Int) {
        event.reply("$number * pi = ${number * PI}").queue()
    }
}