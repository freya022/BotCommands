package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent

@CommandMarker
class SlashVararg {
    @CommandMarker
    fun varArgAggregator(amount: Int, vararg args: Int): List<Int?> = args.toList().let {
        when {
            it.size < amount -> it + arrayOfNulls(amount - it.size)
            else -> it
        }
    }

    @CommandMarker
    fun onSlashVararg(event: GuildSlashEvent, ints: List<Int?>) {
        event.reply("ints: $ints").queue()
    }

    @AppDeclaration
    fun declare(guildApplicationCommandManager: GuildApplicationCommandManager) {
        guildApplicationCommandManager.slashCommand("test", scope = CommandScope.GUILD, ::onSlashVararg) {
            aggregate("ints", ::varArgAggregator) {
                generatedOption("amount") { 2 }

                //TODO issue with the varargs use case, is that the declared name will appear multiple times
                // one solution could be to instead have a Map<DeclaredName, List<OptionBuilder>>
                option("args", "arg_1") {
                    description = "1st arg"
                }

                option("args", "arg_2") {
                    description = "2nd arg"
                }
            }
        }
    }
}