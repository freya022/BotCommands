package com.freya02.botcommands.test.commands2

import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.ApplicationCommandManager
import com.freya02.botcommands.api.application.CommandPath
import com.freya02.botcommands.api.application.slash.GuildSlashEvent

fun interface Lol {
    fun BContext.xd()
}

class MyCommand {
    fun test(t: Lol) {}

    fun executeCommand(event: GuildSlashEvent) {

    }

    @Declaration
    fun declare(context: BContext, manager: ApplicationCommandManager) {
        test {}

        manager.slashCommand(CommandPath.of("my_command"), this) {
            description = "mah desc"

            function = ::executeCommand
        }
    }
}