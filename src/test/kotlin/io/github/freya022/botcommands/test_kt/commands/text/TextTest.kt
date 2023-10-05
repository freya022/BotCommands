package io.github.freya022.botcommands.test_kt.commands.text

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.prefixed.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.prefixed.TextCommand
import io.github.freya022.botcommands.api.commands.prefixed.TextCommandManager
import io.github.freya022.botcommands.api.commands.prefixed.TextGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.prefixed.annotations.JDATextCommand
import io.github.freya022.botcommands.api.commands.prefixed.annotations.TextDeclaration
import io.github.freya022.botcommands.api.commands.prefixed.annotations.TextOption
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.parameters.ParameterType

@Command
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
            variation(::onTextTest) {
                option("text")

                customOption("context")

                generatedOption("userName") {
                    it.author.name
                }
            }

            variation(::onTextTestFallback) {
                customOption("context")

                generatedOption("userName") {
                    it.author.name
                }
            }

            subcommand("subcommand") {
                variation(::onTextTestSubcommand) {
                    option("number")
                }
            }
        }
    }
}