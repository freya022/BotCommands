package io.github.freya022.bot.commands.text

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.TextCommand
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand
import com.freya02.botcommands.api.commands.prefixed.annotations.TextOption
import kotlin.math.PI

@Command
class TextNumber : TextCommand() {
    @JDATextCommand(name = "number")
    fun onTextNumber(event: BaseCommandEvent, @TextOption number: Int) {
        event.reply("$number * pi = ${number * PI}").queue()
    }
}