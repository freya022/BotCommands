package com.freya02.botcommands.test_kt.commands.text

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.TextCommandManager
import com.freya02.botcommands.api.commands.prefixed.annotations.TextDeclaration

@Command
class TextVararg {
    @CommandMarker
    fun onTextVararg(event: BaseCommandEvent, ints: List<Int>) {
        event.respond("ints: $ints").queue()
    }

    @TextDeclaration
    fun declare(commandManager: TextCommandManager) {
        commandManager.textCommand("vararg") {
            variation(::onTextVararg) {
                //Only 1 vararg supported
                optionVararg("ints", 4, 1, { i -> "arg_1_$i" })
            }
        }
    }
}