package io.github.freya022.botcommands.test.commands.slash

import io.github.freya022.botcommands.api.commands.annotations.Command
import io.github.freya022.botcommands.api.commands.annotations.VarArgs
import io.github.freya022.botcommands.api.commands.application.ApplicationCommand
import io.github.freya022.botcommands.api.commands.application.CommandScope
import io.github.freya022.botcommands.api.commands.application.annotations.AppDeclaration
import io.github.freya022.botcommands.api.commands.application.declaration.GuildApplicationCommandManager
import io.github.freya022.botcommands.api.commands.application.slash.GuildSlashEvent
import io.github.freya022.botcommands.api.commands.application.slash.annotations.JDASlashCommand
import io.github.freya022.botcommands.api.commands.application.slash.annotations.SlashOption

@Command
class SlashVararg : ApplicationCommand() {
    @JDASlashCommand(name = "vararg_annotated")
    fun onSlashVararg(
        event: GuildSlashEvent,
        @SlashOption(name = "arg_1", description = "arg of 1st group") @VarArgs(2, numRequired = 1) ints: List<Int>,
        @SlashOption(name = "arg_2", description = "arg of 2nd group") @VarArgs(2, numRequired = 1) ints2: List<Int>
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