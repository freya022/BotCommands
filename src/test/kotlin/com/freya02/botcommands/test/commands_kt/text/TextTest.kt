package com.freya02.botcommands.test.commands_kt.text

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.annotations.GeneratedOption
import com.freya02.botcommands.api.commands.prefixed.BaseCommandEvent
import com.freya02.botcommands.api.commands.prefixed.TextCommand
import com.freya02.botcommands.api.commands.prefixed.TextCommandManager
import com.freya02.botcommands.api.commands.prefixed.TextGeneratedValueSupplier
import com.freya02.botcommands.api.commands.prefixed.annotations.JDATextCommand
import com.freya02.botcommands.api.commands.prefixed.annotations.TextDeclaration
import com.freya02.botcommands.api.commands.prefixed.annotations.TextOption
import com.freya02.botcommands.api.parameters.ParameterType

@CommandMarker
class TextTest : TextCommand() {
    override fun getGeneratedValueSupplier(
        commandPath: CommandPath,
        optionName: String,
        parameterType: ParameterType
    ): TextGeneratedValueSupplier {
        if (optionName ==  "user_name") {
            return TextGeneratedValueSupplier {
                event -> event.author.name
            }
        }

        return super.getGeneratedValueSupplier(commandPath, optionName, parameterType)
    }

    @JDATextCommand(name = "test_annotated")
    fun onTextTestFallback(
        event: BaseCommandEvent,
        context: BContext,
        @GeneratedOption userName: String
    ) {
        event.reply("""
            text: [fallback]
            context: $context
            user name: $userName
        """.trimIndent()).queue()
    }

    @JDATextCommand(name = "test_annotated")
    fun onTextTest(
        event: BaseCommandEvent,
        @TextOption text: String,
        context: BContext,
        @GeneratedOption userName: String
    ) {
        event.reply("""
            text: $text
            context: $context
            user name: $userName
        """.trimIndent()).queue()
    }

    @JDATextCommand(name = "test_annotated", subcommand = "subcommand")
    fun onTextTestSubcommand(
        event: BaseCommandEvent,
        @TextOption number: Double
    ) {
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