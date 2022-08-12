package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.annotations.api.application.slash.annotations.ChannelTypes
import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.annotations.Name
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.ValueRange.Companion.range
import com.freya02.botcommands.api.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.application.slash.autocomplete.AutocompleteCacheMode
import com.freya02.botcommands.internal.enumSetOf
import net.dv8tion.jda.api.entities.Category
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice

class MyCommand : ApplicationCommand() {
    @CommandMarker
    fun executeCommand(
        event: GuildSlashEvent,
        stringOption: String,
        @Name("int", "notIntOption") intOption: Int,
        @Name(name = "user") userOption: User,
        @ChannelTypes(ChannelType.CATEGORY) channelOptionAnnot: Category,
        channelOption: TextChannel,
        autocompleteStr: String,
        @Name(declaredName = "notDoubleOption") doubleOption: Double?,
        custom: BContext,
        guildName: String
    ) {
        event.reply("""
                    event: $event
                    string: $stringOption
                    int: $intOption
                    user: $userOption
                    category: $channelOptionAnnot
                    text channel: $channelOption
                    autocomplete string: $autocompleteStr
                    double: $doubleOption
                    custom: $custom
                    [generated] guild name: $guildName
        """.trimIndent()).queue()
    }

    @CommandMarker
    fun runAutocomplete(
        event: CommandAutoCompleteInteractionEvent,
        stringOption: String,
        @Name(declaredName = "notDoubleOption") doubleOption: Double?
    ): Collection<Choice> {
        println("ran")

        return listOf(Choice("lol name + $stringOption + $doubleOption", "lol value + $stringOption + $doubleOption"))
    }

    @Declaration
    fun declare(manager: GlobalApplicationCommandManager) {
        for ((subname, localFunction) in mapOf("kt" to ::executeCommand, "java" to MyJavaCommand::cmd)) {
            manager.slashCommand(CommandPath.of("my_command", subname)) {
                description = "mah desc"

                option("stringOption") {
                    description = "Option description"

                    choices = listOf(Choice("a", "a"), Choice("b", "b"), Choice("c", "c"))
                }

                option("notIntOption") {
                    description = "An integer"

                    valueRange = 1 range 2

                    choices = listOf(Choice("1", 1L), Choice("2", 2L))
                }

                option("notDoubleOption") {
                    description = "A double"
                }

                option("userOption") {
                    description = "An user"
                }

                option("channelOptionAnnot")

                option("channelOption") {
                    channelTypes = enumSetOf(ChannelType.TEXT)
                }

                customOption("custom")

                option("autocompleteStr") {
                    description = "Autocomplete !"

                    autocomplete {
                        function = ::runAutocomplete

                        cache {
                            cacheMode = AutocompleteCacheMode.CONSTANT_BY_KEY
                        }
                    }
                }

                generatedOption("guildName") {
                    it.guild!!.name
                }

                function = localFunction
            }
        }
    }
}