package com.freya02.botcommands.test.commands_kt.slash

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.CommandPath
import com.freya02.botcommands.api.commands.annotations.GeneratedOption
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.ValueRange.Companion.range
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheMode
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import com.freya02.botcommands.api.commands.application.slash.autocomplete.annotations.CacheAutocomplete
import com.freya02.botcommands.api.parameters.ParameterType
import com.freya02.botcommands.internal.enumSetOf
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice

@CommandMarker
class SlashMyCommand : ApplicationCommand() {
    override fun getOptionChoices(guild: Guild?, commandPath: CommandPath, optionIndex: Int): List<Choice> {
        if (optionIndex == 0) {
            return listOf(Choice("a", "a"), Choice("b", "b"), Choice("c", "c"))
        } else if (optionIndex == 1) {
            return listOf(Choice("1", 1L), Choice("2", 2L))
        }

        return super.getOptionChoices(guild, commandPath, optionIndex)
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

    @com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand(name = "my_command_annotated", subcommand = "kt", description = "mah desc")
    fun executeCommand(
        event: GuildSlashEvent,
        @com.freya02.botcommands.api.commands.application.annotations.AppOption(name = "string_annotated", description = "Option description") stringOption: String,
        @com.freya02.botcommands.api.commands.application.annotations.AppOption(name = "int_annotated", description = "An integer") @com.freya02.botcommands.api.commands.application.slash.annotations.LongRange(from = 1, to = 2) intOption: Int,
        @com.freya02.botcommands.api.commands.application.annotations.AppOption(name = "user_annotated", description = "An user") userOption: User,
        @com.freya02.botcommands.api.commands.application.annotations.AppOption(name = "channel_annot_annotated") @com.freya02.botcommands.api.commands.application.slash.annotations.ChannelTypes(ChannelType.CATEGORY) channelOptionAnnot: Category,
        @com.freya02.botcommands.api.commands.application.annotations.AppOption(name = "channel_annotated") channelOption: TextChannel,
        @com.freya02.botcommands.api.commands.application.annotations.AppOption(name = "autocomplete_str_annotated", description = "Autocomplete !", autocomplete = autocompleteHandlerName) autocompleteStr: String,
        @com.freya02.botcommands.api.commands.application.annotations.AppOption(name = "double_annotated", description = "A double") doubleOption: Double?,
        custom: BContext,
        @GeneratedOption guildName: String
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
    fun declare(manager: com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager) {
        for ((subname, localFunction) in mapOf("kt" to ::executeCommand, "java" to SlashMyJavaCommand::cmd)) {
            manager.slashCommand(CommandPath.of("my_command", subname)) {
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

    companion object {
        const val autocompleteHandlerName = "MyCommand: autocompleteStr"
    }
}