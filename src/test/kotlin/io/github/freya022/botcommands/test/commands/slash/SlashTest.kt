package io.github.freya022.botcommands.test.commands.slash

import dev.minn.jda.ktx.messages.reply_
import io.github.freya022.botcommands.api.commands.CommandPath
import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.GeneratedOption
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.slash.ApplicationGeneratedValueSupplier
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDATopLevelSlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption
import io.github.freya022.botcommands.api.commands.application.slash.autocomplete.annotations.AutocompleteHandler
import io.github.freya022.botcommands.api.core.reflect.ParameterType
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent
import net.dv8tion.jda.api.interactions.commands.Command.Choice

private const val guildNicknameAutocompleteName = "NewSlashTest: guildNickname"

@Command
class SlashTest : ApplicationCommand() {
    override fun getGeneratedValueSupplier(
        guild: Guild?, commandId: String?,
        commandPath: CommandPath, optionName: String,
        parameterType: ParameterType
    ): ApplicationGeneratedValueSupplier {
        if (optionName == "guild_name") {
            return ApplicationGeneratedValueSupplier { it.guild!!.name }
        }

        return super.getGeneratedValueSupplier(guild, commandId, commandPath, optionName, parameterType)
    }

    @JDASlashCommand(name = "test_annotated")
    @JDATopLevelSlashCommand(scope = CommandScope.GUILD)
    fun onSlashTest(
        event: GuildSlashEvent,
        @SlashOption(autocomplete = guildNicknameAutocompleteName) guildNickname: String,
        @GeneratedOption guildName: String
    ) {
        event.reply_("woo in $guildName ($guildNickname)", ephemeral = true).queue()
    }

    @AutocompleteHandler(name = guildNicknameAutocompleteName)
    fun onSlashTestGuildNicknameAutocomplete(
        event: CommandAutoCompleteInteractionEvent,
        guildName: String, //Generated
        guildNickname: String //User supplied
    ): List<Choice> {
        return listOf("${guildName}_nick ($guildNickname)").map { Choice(it, it) }
    }

    @AppDeclaration
    fun declare(guildApplicationCommandManager: GuildApplicationCommandManager) {
        guildApplicationCommandManager.slashCommand("test", scope = CommandScope.GUILD, ::onSlashTest) {
            option("guildNickname")

            generatedOption("guildName") {
                it.guild!!.name
            }
        }
    }
}