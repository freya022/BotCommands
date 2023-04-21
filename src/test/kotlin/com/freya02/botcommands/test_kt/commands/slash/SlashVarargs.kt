package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.VarArgs
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent

@CommandMarker
class SlashVarargs : ApplicationCommand() {
    @JDASlashCommand(name = "varargs_annotated")
    fun onSlashVarargs(event: GuildSlashEvent, @AppOption @VarArgs(3, numRequired = 1) list: List<Int?>) {
        event.reply_("ok $list", ephemeral = true).queue()
    }

    @CommandMarker
    fun onVarargsAutocomplete(event: CommandAutoCompleteInteractionEvent, @AppOption list: List<Int?>): List<String> {
        return list.filterNotNull().map { it + 1 }.map { it.toString() }
    }

    @AppDeclaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.slashCommand("varargs") {
            option("list") {
                varArgs = 3
                requiredVarArgs = 1

                autocomplete("SlashVarargs: onVarargsAutocomplete") {
                    showUserInput = false
                    function = ::onVarargsAutocomplete
                }
            }

            function = ::onSlashVarargs
        }
    }
}