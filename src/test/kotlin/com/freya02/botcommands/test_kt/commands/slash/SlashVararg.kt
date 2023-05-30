package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.commands.annotations.Command
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.CommandScope
import com.freya02.botcommands.api.commands.application.GuildApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.annotations.AppOption
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import com.freya02.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import com.freya02.botcommands.api.commands.application.slash.annotations.VarArgs

@Command
class SlashVararg : ApplicationCommand() {
    @JDASlashCommand(name = "vararg_annotated")
    fun onSlashVararg(
        event: GuildSlashEvent,
        @AppOption(name = "arg_1", description = "arg of 1st group") @VarArgs(2, numRequired = 1) ints: List<Int>,
        @AppOption(name = "arg_2", description = "arg of 2nd group") @VarArgs(2, numRequired = 1) ints2: List<Int>
    ) {
        event.reply("ints: $ints, ints2: $ints2").queue()
    }

    @AppDeclaration
    fun declare(guildApplicationCommandManager: GuildApplicationCommandManager) {
        guildApplicationCommandManager.slashCommand("vararg", scope = CommandScope.GUILD, ::onSlashVararg) {
            optionVararg("ints", 2, 1, { i -> "arg_1_$i" }) { i ->
                description = "arg #$i arg of 1st group"
            }

            optionVararg("ints2", 2, 1, { i -> "arg_2_$i" }) { i ->
                description = "arg #$i arg of 2nd group"
            }
        }
    }
}