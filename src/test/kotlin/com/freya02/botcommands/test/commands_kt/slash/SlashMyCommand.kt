package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.annotations.GeneratedOption
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.ValueRange.Companion.range
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.LongRange
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheMode
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import com.freya02.botcommands.api.parameters.ParameterType
import com.freya02.botcommands.internal.enumSetOf
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice

@CommandMarker
class SlashMyCommand : ApplicationCommand() {
    override fun getOptionChoices(guild: Guild?, commandPath: CommandPath, optionName: String): List<Choice> {
        if (optionName == "string_option" || optionName == "string_annotated") {
            return listOf(Choice("a", "a"), Choice("b", "b"), Choice("c", "c"))
        } else if (optionName == "int_option" || optionName == "int_annotated") {
            return listOf(Choice("1", 1L), Choice("2", 2L))
        }

        return super.getOptionChoices(guild, commandPath, optionName)
    }

    override fun getGeneratedValueSupplier(
        guild: Guild?,
        commandId: String?,
        commandPath: CommandPath,
        optionName: String,
        parameterType: ParameterType
    ): ApplicationGeneratedValueSupplier {
        if (optionName == "guild_name") {
            return ApplicationGeneratedValueSupplier {
                it.guild!!.name
            }
        }

        return super.getGeneratedValueSupplier(guild, commandId, commandPath, optionName, parameterType)
    }

    @JDASlashCommand(name = "my_command_annotated", subcommand = "kt", description = "mah desc")
    fun executeCommand(
        event: GuildSlashEvent,
        @AppOption(name = "string_annotated", description = "Option description") stringOption: String,
        @AppOption(name = "int_annotated", description = "An integer") @LongRange(from = 1, to = 2) intOption: Int,
        @AppOption(name = "user_annotated", description = "An user") userOption: User,
        @AppOption(name = "channel_annotated") channelOption: GuildChannel,
        @AppOption(name = "autocomplete_str_annotated", description = "Autocomplete !", autocomplete = autocompleteHandlerName) autocompleteStr: String,
        @AppOption(name = "double_annotated", description = "A double") doubleOption: Double?,
        custom: BContext,
        @GeneratedOption guildName: String
    ) {
        event.reply("""
                    event: $event
                    string: $stringOption
                    int: $intOption
                    user: $userOption
                    text channel: $channelOption
                    autocomplete string: $autocompleteStr
                    double: $doubleOption
                    custom: $custom
                    [generated] guild name: $guildName
        """.trimIndent()).queue()
    }

    @AutocompleteHandler(name = autocompleteHandlerName)
    @CacheAutocomplete(cacheMode = AutocompleteCacheMode.CONSTANT_BY_KEY)
    fun runAutocomplete(
        event: CommandAutoCompleteInteractionEvent,
        stringOption: String,
        doubleOption: Double?
    ): Collection<Choice> {
        println("ran")

        return listOf(Choice("lol name + $stringOption + $doubleOption", "lol value + $stringOption + $doubleOption"))
    }

    @AppDeclaration
    fun declare(manager: GlobalApplicationCommandManager) {
        manager.slashCommand("my_command") {
            for ((subname, localFunction) in mapOf("kt" to ::executeCommand, "java" to SlashMyJavaCommand::cmd)) {
                subcommand(subname) {
                    description = "mah desc"

                    option("stringOption", "string") {
                        description = "Option description"

                        choices = listOf(Choice("a", "a"), Choice("b", "b"), Choice("c", "c"))
                    }

                    option("intOption", "int") {
                        description = "An integer"

                        valueRange = 1 range 2

                        choices = listOf(Choice("1", 1L), Choice("2", 2L))
                    }

                    option("doubleOption", "double") {
                        description = "A double"
                    }

                    option("userOption", "user") {
                        description = "An user"
                    }

                    option("channelOption") {
                        channelTypes = enumSetOf(ChannelType.TEXT)
                    }

                    customOption("custom")

                    option("autocompleteStr") {
                        description = "Autocomplete !"

                        autocomplete("MyCommand: autocompleteStr") {
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

    companion object {
        const val autocompleteHandlerName = "MyCommand: autocompleteStr"
    }
}