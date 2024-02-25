package io.github.freya022.botcommands.test.commands.text

import io.github.freya022.botcommands.api.annotations.CommandMarker
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandManager
import io.github.freya022.botcommands.api.commands.text.provider.TextCommandProvider

@Command
class TextVararg : TextCommandProvider {
    @CommandMarker
    fun onTextVararg(event: BaseCommandEvent, ints: List<Int>) {
        event.respond("ints: $ints").queue()
    }

    override fun declareTextCommands(manager: TextCommandManager) {
        manager.textCommand("vararg") {
            variation(::onTextVararg) {
                //Only 1 vararg supported
                optionVararg("ints", 4, 1, { i -> "arg_1_$i" })
            }
        }
    }
}