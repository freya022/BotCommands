package com.freya02.botcommands.test.commands2

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.api.BContext
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.annotations.Name
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

    @CommandMarker
    fun executeCommand(event: GuildSlashEvent, stringOption: String, @Name("number", "notNumberOption") numberOption: Int, custom: BContext) {
        event.reply(stringOption + numberOption + custom).queue()
    }

    @Declaration
    fun declare(context: BContext, manager: ApplicationCommandManager) {
        test {}

        manager.slashCommand(CommandPath.of("my_command"), this) {
            scope = CommandScope.GUILD
            description = "mah desc"

            option("stringOption") {
                description = "Option description"
            }

            option("notNumberOption") {
                description = "A number"
            }

            customOption("custom")

            function = ::executeCommand
        }
    }
}