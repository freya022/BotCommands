package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent

@CommandMarker
class SlashVararg {
    @CommandMarker
    fun onSlashVararg(event: GuildSlashEvent, ints: List<Int?>, ints2: List<Int?>) {
        event.reply("ints: $ints, ints2: $ints2").queue()
    }

    @AppDeclaration
    fun declare(guildApplicationCommandManager: GuildApplicationCommandManager) {
        guildApplicationCommandManager.slashCommand("vararg", scope = CommandScope.GUILD, ::onSlashVararg) {
            optionVararg("ints", 2, { i -> "arg_1_$i" }) { i ->
                description = "arg #$i arg of 1st group"
            }

            optionVararg("ints2", 2, { i -> "arg_2_$i" }) { i ->
                description = "arg #$i arg of 2nd group"
            }
        }
    }
}