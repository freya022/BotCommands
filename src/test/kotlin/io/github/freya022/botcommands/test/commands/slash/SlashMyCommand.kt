package io.github.freya022.botcommands.test.commands.slash

import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.GlobalApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.ValueRange.Companion.range
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.LongRange
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.annotations.TopLevelSlashCommandData
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteCacheMode
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.AutocompleteManager
import io.github.freya022.botcommands.api.core.BContext
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import io.github.freya022.botcommands.api.core.utils.enumSetOf
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice

@Command
class SlashMyCommand : ApplicationCommand(), AutocompleteDeclaration {
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

    @TopLevelSlashCommandData
    @JDASlashCommand(name = "my_command_annotated", subcommand = "kt", description = "mah desc")
    fun executeCommand(
        event: GuildSlashEvent,
        @SlashOption(name = "string_annotated", description = "Option description") stringOption: String,
        @SlashOption(name = "int_annotated", description = "An integer") @LongRange(from = 1, to = 2) intOption: Int,
        @SlashOption(name = "user_annotated", description = "An user") userOption: User,
        @SlashOption(name = "channel_annotated") channelOption: GuildChannel,
        @SlashOption(name = "autocomplete_str_annotated", description = "Autocomplete !", autocomplete = autocompleteHandlerName) autocompleteStr: String,
        @SlashOption(name = "double_annotated", description = "A double") doubleOption: Double?,
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

//    @AutocompleteHandler(name = autocompleteHandlerName)
//    @CacheAutocomplete(cacheMode = AutocompleteCacheMode.CONSTANT_BY_KEY)
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
        manager.slashCommand("my_command", function = null) {
            for ((subname, localFunction) in mapOf("kt" to ::executeCommand, "java" to SlashMyJavaCommand::cmd)) {
                subcommand(subname, localFunction) {
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

                        autocompleteByFunction(SlashMyJavaCommand::runAutocompleteJava)
                    }

                    generatedOption("guildName") {
                        it.guild!!.name
                    }
                }
            }
        }
    }

    override fun declare(manager: AutocompleteManager) {
        manager.autocomplete(SlashMyJavaCommand::runAutocompleteJava) {
            cache(AutocompleteCacheMode.CONSTANT_BY_KEY)
        }
    }

    companion object {
        const val autocompleteHandlerName = "MyCommand: autocompleteStr"
    }
}
