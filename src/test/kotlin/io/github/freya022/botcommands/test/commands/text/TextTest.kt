package io.github.freya022.botcommands.test.commands.text

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.TextCommandManager
import io.github.freya022.botcommands.api.commands.text.TextGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommand
import io.github.freya022.botcommands.api.commands.text.annotations.TextDeclaration
import io.github.freya022.botcommands.api.commands.text.annotations.TextOption
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.reflect.ParameterType

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

    @JDATextCommand(path = ["test_annotated"])
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

    @JDATextCommand(path = ["test_annotated"])
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

    @JDATextCommand(path = ["test_annotated", "subcommand"])
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