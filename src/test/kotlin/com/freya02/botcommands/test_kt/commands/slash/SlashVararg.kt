package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent

@CommandMarker
class SlashVararg {
    @CommandMarker
    fun varArgAggregator(event: GuildSlashEvent, amount: Int, vararg args: Int): List<Int?> = args.toList().let {
        when {
            it.size < amount -> it + arrayOfNulls(amount - it.size)
            else -> it
        }
    }

    @CommandMarker
    fun onSlashVararg(event: GuildSlashEvent, ints: List<Int?>, ints2: List<Int?>) {
        event.reply("ints: $ints, ints2: $ints2").queue()
    }

    @AppDeclaration
    fun declare(guildApplicationCommandManager: GuildApplicationCommandManager) {
        guildApplicationCommandManager.slashCommand("vararg", scope = CommandScope.GUILD, ::onSlashVararg) {
            aggregate("ints", ::varArgAggregator) {
                generatedOption("amount") { 2 }

                option("args", "arg_1_1") {
                    description = "1st arg of 1st group"
                }

                option("args", "arg_1_2") {
                    description = "2nd arg of 1st group"
                }
            }

            aggregate("ints2", ::varArgAggregator) {
                generatedOption("amount") { 2 }

                option("args", "arg_2_1") {
                    description = "1st arg of 2nd group"
                }

                option("args", "arg_2_2") {
                    description = "2nd arg of 2nd group"
                }
            }
        }
    }
}