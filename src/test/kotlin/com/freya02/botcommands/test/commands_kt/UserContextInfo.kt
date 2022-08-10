package com.freya02.botcommands.test.commands_kt

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.GlobalApplicationCommandManager
import com.freya02.botcommands.api.application.context.user.GlobalUserEvent
import dev.minn.jda.ktx.messages.reply_
import net.dv8tion.jda.api.entities.User

@CommandMarker
class UserContextInfo : ApplicationCommand() {
    @CommandMarker
    fun onUserContextInfo(event: GlobalUserEvent, user: User) {
        event.reply_("User: ${user.asTag}", ephemeral = true).queue()
    }

    @Declaration
    fun declare(globalApplicationCommandManager: GlobalApplicationCommandManager) {
        globalApplicationCommandManager.userCommand("User info") {
            option("user")

            function = ::onUserContextInfo
        }
    }
}