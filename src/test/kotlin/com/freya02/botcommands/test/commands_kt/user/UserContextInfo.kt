package com.freya02.botcommands.test.commands_kt.user

import com.freya02.botcommands.annotations.api.annotations.CommandMarker
import com.freya02.botcommands.api.annotations.Declaration
import com.freya02.botcommands.api.application.ApplicationCommand
import com.freya02.botcommands.api.application.CommandScope
import com.freya02.botcommands.api.application.GuildApplicationCommandManager
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent

@CommandMarker
class UserContextInfo : ApplicationCommand() {
    @Declaration
    fun declare(guildApplicationCommandManager: GuildApplicationCommandManager) {
        guildApplicationCommandManager.userCommand("User info", CommandScope.GUILD) {
            option("user")

            generatedOption("userTag") {
                it as UserContextInteractionEvent

                it.target.asTag
            }

            function = UserContextInfoAnnotated::onUserContextInfo
        }
    }
}