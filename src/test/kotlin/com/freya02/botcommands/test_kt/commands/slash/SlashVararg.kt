package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandInfo
import com.freya02.botcommands.internal.commands.application.slash.SlashCommandParameter
import net.dv8tion.jda.api.interactions.commands.CommandInteractionPayload
import net.dv8tion.jda.api.interactions.commands.OptionMapping

class SlashVararg {
    //TODO to be removed, see SlashCommandOptionAggregateBuilder#aggregator
    suspend fun varArgSolver(
        context: BContext,
        info: SlashCommandInfo,
        event: CommandInteractionPayload,
        mappings: Map<String, OptionMapping>,
        commandParameter: SlashCommandParameter
    ): List<Any?> {
        return mappings.map { (_, mapping) -> commandParameter.resolver.resolveSuspend(context, info, event, mapping) }
    }

    fun onSlashVararg(event: GuildSlashEvent, args: List<Int>) {

    }

    @AppDeclaration
    fun declare(guildApplicationCommandManager: GuildApplicationCommandManager) {
        guildApplicationCommandManager.slashCommand("test", scope = CommandScope.GUILD) {
            aggregate("args") {
                option("unused", "arg_1") {
                    description = "1st arg"
                }

                option("unused", "arg_2") {
                    description = "2nd arg"
                }

                aggregator = ::varArgSolver
            }

            generatedOption("guildName") {
                it.guild!!.name
            }

            function = ::onSlashVararg
        }
    }
}