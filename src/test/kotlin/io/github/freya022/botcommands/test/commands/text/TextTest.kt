package io.github.freya022.botcommands.test.commands.text

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.text.BaseCommandEvent
import io.github.freya022.botcommands.api.commands.text.TextCommand
import io.github.freya022.botcommands.api.commands.text.TextGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.text.annotations.Hidden
import io.github.freya022.botcommands.api.commands.text.annotations.JDATextCommandVariation
import io.github.freya022.botcommands.api.commands.text.annotations.TextCommandData
import io.github.freya022.botcommands.api.commands.text.annotations.TextOption
import io.github.freya022.botcommands.api.commands.text.declaration.TextCommandManager
import io.github.freya022.botcommands.api.commands.text.declaration.TextCommandsDeclaration
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.reflect.ParameterType

@Command
class TextTest : TextCommand(), TextCommandsDeclaration {
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

    @JDATextCommandVariation(path = ["test_annotated"], description = "Fallback variation description")
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

    @TextCommandData(description = "'test_annotated' command description")
    @JDATextCommandVariation(path = ["test_annotated"], description = "First variation description")
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

    @TextCommandData(description = "'test_annotated subcommand' subcommand description")
    @JDATextCommandVariation(path = ["test_annotated", "subcommand"], description = "First subcommand variation description")
    fun onTextTestSubcommand(
        event: BaseCommandEvent,
        @TextOption number: Double
    ) {
        event.reply("""
            number: $number
        """.trimIndent()).queue()
    }

    @Hidden
    @TextCommandData(description = "'test_annotated subcommand hidden' hidden subcommand description")
    @JDATextCommandVariation(path = ["test_annotated", "subcommand", "hidden"], description = "First hidden subcommand variation description")
    fun onTextTextSubcommandHidden(event: BaseCommandEvent) {
        event.reply(":spy:").queue()
    }

    override fun declareTextCommands(manager: TextCommandManager) {
        manager.textCommand("test") {
            description = "'test' command description"

            variation(::onTextTest) {
                description = "First variation description"

                option("text")

                customOption("context")

                generatedOption("userName") {
                    it.author.name
                }
            }

            variation(::onTextTestFallback) {
                description = "Fallback variation description"

                customOption("context")

                generatedOption("userName") {
                    it.author.name
                }
            }

            subcommand("subcommand") {
                description = "'test subcommand' subcommand description"

                variation(::onTextTestSubcommand) {
                    description = "First subcommand variation description"

                    option("number")
                }

                subcommand("hidden") {
                    description = "'test subcommand hidden' hidden subcommand description"

                    hidden = true

                    variation(::onTextTextSubcommandHidden) {
                        description = "First hidden subcommand variation description"
                    }
                }
            }
        }
    }
}