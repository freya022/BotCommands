package com.freya02.botcommands.test_kt.commands.slash

import com.freya02.botcommands.api.annotations.CommandMarker
import com.freya02.botcommands.api.commands.application.ApplicationCommand
import com.freya02.botcommands.api.commands.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.commands.application.annotations.AppDeclaration
import com.freya02.botcommands.api.commands.application.slash.GuildSlashEvent
import dev.minn.jda.ktx.messages.reply_

@CommandMarker
class SlashVarargs : ApplicationCommand() {
    fun onSlashVarargs(event: GuildSlashEvent, list: List<Int>) {
        event.reply_("ok $list", ephemeral = true).queue()
    }

    @AppDeclaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.slashCommand("varargs") {
            option("list") {
                varArgs = 3
                requiredVarArgs = 3
            }

            function = ::onSlashVarargs
        }
    }
}