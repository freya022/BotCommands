package com.freya02.botcommands.test.commands_kt.text

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.annotations.TextDeclaration
import com.freya02.botcommands.api.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.prefixed.TextCommand
import com.freya02.botcommands.api.prefixed.builder.TextCommandManager

@CommandMarker
class TextTest : TextCommand() {
    @CommandMarker
    fun onTextTestFallback(event: BaseCommandEvent, context: BContext, userName: String) {
        event.reply("""
            text: [fallback]
            context: $context
            user name: $userName
        """.trimIndent()).queue()
    }

    @CommandMarker
    fun onTextTest(event: BaseCommandEvent, text: String, context: BContext, userName: String) {
        event.reply("""
            text: $text
            context: $context
            user name: $userName
        """.trimIndent()).queue()
    }

    @CommandMarker
    fun onTextTestSubcommand(event: BaseCommandEvent, number: Double) {
        event.reply("""
            number: $number
        """.trimIndent()).queue()
    }

    @TextDeclaration
    fun declare(textCommandManager: TextCommandManager) {
        textCommandManager.textCommand("test") {
            option("text")

            customOption("context")

            generatedOption("userName") {
                it.author.name
            }

            function = ::onTextTest
        }

        textCommandManager.textCommand("test") {
            customOption("context")

            generatedOption("userName") {
                it.author.name
            }

            function = ::onTextTestFallback
        }

        textCommandManager.textCommand("test", subcommand = "subcommand") {
            option("number")

            function = ::onTextTestSubcommand
        }
    }
}