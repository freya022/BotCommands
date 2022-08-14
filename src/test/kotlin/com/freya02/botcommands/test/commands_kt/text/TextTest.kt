package com.freya02.botcommands.test.commands_kt.text

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.api.annotations.TextDeclaration
import com.freya02.botcommands.api.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.prefixed.TextCommand
import com.freya02.botcommands.api.prefixed.builder.TextCommandManager

@CommandMarker
class TextTest : TextCommand() {
    @CommandMarker
    fun onTextTest(event: BaseCommandEvent) {
        event.reply("gud").queue()
    }

    @TextDeclaration
    fun declare(textCommandManager: TextCommandManager) {
        textCommandManager.textCommand("test") {
            function = ::onTextTest
        }
    }
}