package com.freya02.botcommands.test.commands2

import com.freya02.botcommands.annotations.api.application.annotations.AppOption
import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.ApplicationCommandManager
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.application.slash.GuildSlashEvent

fun interface Lol {
    fun BContext.xd()
}

class MyCommand : ApplicationCommand() {
    fun test(t: Lol) {}

    fun executeCommand(event: GuildSlashEvent, @AppOption opt: String) {
        event.reply(opt).queue()
    }

    @Declaration
    fun declare(context: BContext, manager: ApplicationCommandManager) {
        test {}

        manager.slashCommand(CommandPath.of("my_command"), this) {
            scope = CommandScope.GUILD
            description = "mah desc"

            function = ::executeCommand
        }
    }
}