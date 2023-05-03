package com.freya02.botcommands.test_kt.commands.text

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.TextCommandManager
import com.freya02.botcommands.api.commands.prefixed.annotations.TextDeclaration

@CommandMarker
class TextVararg {
    @CommandMarker
    fun onTextVararg(event: BaseCommandEvent, ints: List<Int?>, ints2: List<Int?>) {
        event.respond("ints: $ints, ints2: $ints2").queue()
    }

    @TextDeclaration
    fun declare(commandManager: TextCommandManager) {
        commandManager.textCommand("vararg") {
            variation(::onTextVararg) {
                optionVararg("ints", 2, { i -> "arg_1_$i" })

                optionVararg("ints2", 2, { i -> "arg_2_$i" })
            }
        }
    }
}